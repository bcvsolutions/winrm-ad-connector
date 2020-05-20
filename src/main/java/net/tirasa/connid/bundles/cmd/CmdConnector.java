/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
 * Copyright (C) 2020 BCV solutions s.r.o., Czech Republic (http://www.bcvsolutions.eu)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.cmd;

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeBuilder;
import org.identityconnectors.framework.common.objects.AttributeInfoBuilder;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ConnectorObject;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ObjectClassInfoBuilder;
import org.identityconnectors.framework.common.objects.OperationOptions;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Schema;
import org.identityconnectors.framework.common.objects.SchemaBuilder;
import org.identityconnectors.framework.common.objects.SyncResultsHandler;
import org.identityconnectors.framework.common.objects.SyncToken;
import org.identityconnectors.framework.common.objects.Uid;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.AuthenticateOp;
import org.identityconnectors.framework.spi.operations.CreateOp;
import org.identityconnectors.framework.spi.operations.DeleteOp;
import org.identityconnectors.framework.spi.operations.ResolveUsernameOp;
import org.identityconnectors.framework.spi.operations.SchemaOp;
import org.identityconnectors.framework.spi.operations.SearchOp;
import org.identityconnectors.framework.spi.operations.SyncOp;
import org.identityconnectors.framework.spi.operations.TestOp;
import org.identityconnectors.framework.spi.operations.UpdateAttributeValuesOp;

import net.tirasa.connid.bundles.ad.ADConnector;
import net.tirasa.connid.bundles.cmd.methods.CmdCreate;
import net.tirasa.connid.bundles.cmd.methods.CmdDelete;
import net.tirasa.connid.bundles.cmd.methods.CmdExecuteQuery;
import net.tirasa.connid.bundles.cmd.methods.CmdTest;
import net.tirasa.connid.bundles.cmd.methods.CmdUpdate;
import net.tirasa.connid.bundles.cmd.search.Operand;
import net.tirasa.connid.bundles.cmd.search.Operator;
import net.tirasa.connid.bundles.cmd.util.GuardedStringAccessor;
import net.tirasa.connid.bundles.ldap.search.LdapFilter;

@ConnectorClass(configurationClass = CmdConfiguration.class, displayNameKey = "cmd.connector.display")
public class CmdConnector implements TestOp, PoolableConnector, SchemaOp, SearchOp<LdapFilter>, AuthenticateOp,
		ResolveUsernameOp, CreateOp, UpdateAttributeValuesOp, DeleteOp, SyncOp {

	private static final Log LOG = Log.getLog(CmdConnector.class);
	private static final String SCRIPT_ATT_NAME = "script";
	private static final String ATT_FOR_WINRM = "attributesForWinRM";

	private CmdConfiguration cmdConfiguration;
	private ADConnector adConnector = new ADConnector();

	@Override
	public Configuration getConfiguration() {
		return cmdConfiguration;
	}

	@Override
	public void init(final Configuration configuration) {
		cmdConfiguration = (CmdConfiguration) configuration;
		adConnector.init(configuration);
	}

	@Override
	public void dispose() {
		adConnector.dispose();
	}

	@Override
	public Uid create(final ObjectClass oc, final Set<Attribute> attributes, final OperationOptions oo) {
		Uid uidAd = null;
		Uid uidWinrm = null;

		logOk(oc, attributes, oo, "Create");

		Set<Attribute> adAttributes = new HashSet<>(attributes);
		if (cmdConfiguration.isUseWinRMForLdapGroups()) {
			LOG.info("Update of ldapGroups only via WinRM is on, removing ldapGroups from attributes for AD");
			removeAttribute(adAttributes, "ldapGroups");
		}
		LOG.info("Remove attributes ldapGroupsToAdd, ldapGroupsToRemove if they are there for AD");
		removeAttribute(adAttributes, "ldapGroupsToAdd");
		removeAttribute(adAttributes, "ldapGroupsToRemove");

		Set<Attribute> winrmAttributes = copyAttributesAndAddAdditionalCreds(attributes);

		removeWinrmAttributes(oo, adAttributes);

		if (executeAdThenWinrm()) {
			uidAd = adConnector.create(oc, adAttributes, oo);
			LOG.info("Create on AD was executed");
			uidWinrm = createWinrm(oc, winrmAttributes, oo);
			LOG.info("Create via WinRM was executed");
		} else if (executeWinrmThenAd()) {
			uidWinrm = createWinrm(oc, winrmAttributes, oo);
			LOG.info("Create via WinRM was executed");
			uidAd = adConnector.create(oc, adAttributes, oo);
			LOG.info("Create on AD was executed");
		} else if (executeOnlyAd()) {
			uidAd = adConnector.create(oc, adAttributes, oo);
		} else if (executeOnlyWinrm()) {
			uidWinrm = createWinrm(oc, winrmAttributes, oo);
		}

		return checkAndGetUid(uidAd, uidWinrm, "create");
	}

	@Override
	public Uid update(final ObjectClass oc, final Uid uid, final Set<Attribute> attributes, final OperationOptions oo) {
		Uid uidAd = null;
		Uid uidWinrm = null;

		logOk(oc, attributes, oo, "Update");

		Set<Attribute> adAttributes = new HashSet<>(attributes);
		if (cmdConfiguration.isUseWinRMForLdapGroups()) {
			LOG.info("Update of ldapGroups only via WinRM is on, removing ldapGroups from attributes for AD, user:{0}", uid);
			removeAttribute(adAttributes, "ldapGroups");
		}
		LOG.info("Remove attributes ldapGroupsToAdd, ldapGroupsToRemove if they are there for AD, user:{0}", uid);
		removeAttribute(adAttributes, "ldapGroupsToAdd");
		removeAttribute(adAttributes, "ldapGroupsToRemove");
		LOG.info("Remove domain:* attributes which contain credentials for different domain if they are there for AD, user:{0}", uid);
		removeCredentialsAttributes(adAttributes);

		Set<Attribute> winrmAttributes = copyAttributesAndAddAdditionalCreds(attributes);

		removeWinrmAttributes(oo, adAttributes);

		if (executeAdThenWinrm()) {
			uidAd = adConnector.update(oc, uid, adAttributes, oo);
			LOG.info("Update on AD was executed");
			uidWinrm = updateWinrm(oc, uid, winrmAttributes);
			LOG.info("Update via WinRM was executed");
		} else if (executeWinrmThenAd()) {
			uidWinrm = updateWinrm(oc, uid, winrmAttributes);
			LOG.info("Update via WinRM was executed");
			uidAd = adConnector.update(oc, uid, adAttributes, oo);
			LOG.info("Update on AD was executed");
		} else if (executeOnlyAd()) {
			uidAd = adConnector.update(oc, uid, adAttributes, oo);
		} else if (executeOnlyWinrm()) {
			uidWinrm = updateWinrm(oc, uid, winrmAttributes);
		}

		return checkAndGetUid(uidAd, uidWinrm, "update");
	}

	private boolean executeAdThenWinrm() {
		return cmdConfiguration.isCreateViaAd() && !cmdConfiguration.isFirstCreateWinRM() && cmdConfiguration.isCreateViaWinRM();
	}

	private boolean executeWinrmThenAd() {
		return cmdConfiguration.isCreateViaAd() && cmdConfiguration.isFirstCreateWinRM() && cmdConfiguration.isCreateViaWinRM();
	}

	private boolean executeOnlyAd() {
		return cmdConfiguration.isCreateViaAd() && !cmdConfiguration.isCreateViaWinRM();
	}

	private boolean executeOnlyWinrm() {
		return !cmdConfiguration.isCreateViaAd() && cmdConfiguration.isCreateViaWinRM();
	}

	private void logOk(ObjectClass oc, Set<Attribute> attributes, OperationOptions oo, String operation) {
		if (LOG.isOk()) {
			LOG.ok(operation + " parameters:");
			LOG.ok("ObjectClass {0}", oc.getObjectClassValue());

			for (Attribute attr : attributes) {
				LOG.ok("Attribute {0}: {1}", attr.getName(), attr.getValue());
			}
			if (oo != null) {
				for (Map.Entry<String, Object> entrySet : oo.getOptions().entrySet()) {
					final String key = entrySet.getKey();
					final Object value = entrySet.getValue();
					LOG.ok("OperationOptions {0}: {1}", key, value);
				}
			}
		}
	}

	private void removeWinrmAttributes(OperationOptions oo, Set<Attribute> attributes) {
		if (oo != null && oo.getOptions() != null && oo.getOptions().containsKey(ATT_FOR_WINRM)) {
			LOG.info("We will filter AD attributes, because operation options are set");
			// array or simple string
			Object value = oo.getOptions().get(ATT_FOR_WINRM);
			if (value instanceof String) {
				removeAttribute(attributes, String.valueOf(value));
			} else if (value instanceof String[]) {
				List<String> attributesForWinRM = Arrays.asList((String[]) oo.getOptions().get(ATT_FOR_WINRM));
				attributesForWinRM.forEach(nameForRemove -> removeAttribute(attributes, nameForRemove));
			} else {
				LOG.info("Config is not String nor String[] can't perform filtering");
			}
		}
	}

	private Uid createWinrm(ObjectClass oc, Set<Attribute> attributes, OperationOptions oo) {
		Set<Attribute> attributesWithConfig = setConfToAttrs(attributes);
		attributesWithConfig.add(AttributeBuilder.build(SCRIPT_ATT_NAME, cmdConfiguration.getCreatePsPath()));
		return new CmdCreate(oc, cmdConfiguration.getCreateCmdPath(), attributesWithConfig).execCreateCmd();
	}

	private Set<Attribute> copyAttributesAndAddAdditionalCreds(Set<Attribute> attributes) {
		LOG.info("Adding additional creds from config to script attributes");
		Set<Attribute> resultAttributes = new HashSet<>(attributes);
		resultAttributes.add(AttributeBuilder.build("additionalCreds", Arrays.stream(cmdConfiguration.getAdditionalCreds()).map(this::getPassword).collect(Collectors.toList())));
		return resultAttributes;
	}

	private void removeAttribute(Set<Attribute> attributes, String attrName) {
		Attribute attribute = AttributeUtil.find(attrName, attributes);
		if (attribute != null) {
			attributes.remove(attribute);
		}
	}

	private void removeCredentialsAttributes(Set<Attribute> attributes) {
		List<Attribute> credAttributes = attributes.stream().filter(attribute -> attribute.getName().startsWith("domain:")).collect(Collectors.toList());
		credAttributes.forEach(attributes::remove);
	}

	private Uid updateWinrm(ObjectClass oc, Uid uid, Set<Attribute> attributes) {
		Set<Attribute> attributesWithConfig = setConfToAttrs(attributes);
		attributesWithConfig.add(AttributeBuilder.build(SCRIPT_ATT_NAME, cmdConfiguration.getUpdatePsPath()));
		return new CmdUpdate(oc, cmdConfiguration.getUpdateCmdPath(), uid, attributesWithConfig).execUpdateCmd();
	}

	@Override
	public void delete(final ObjectClass oc, final Uid uid, final OperationOptions oo) {
		if (LOG.isOk()) {
			LOG.ok("Delete parameters:");
			LOG.ok("ObjectClass {0}", oc.getObjectClassValue());
			LOG.ok("Uid: {0}", uid.getUidValue());
			if (oo != null) {
				for (Map.Entry<String, Object> entrySet : oo.getOptions().entrySet()) {
					LOG.ok("OperationOptions {0}: {1}", entrySet.getKey(), entrySet.getValue());
				}
			}
		}

		if (cmdConfiguration.isDeleteViaAd() && !cmdConfiguration.isFirstDeleteWinRM() && cmdConfiguration.isDeleteViaWinRM()) {
			adConnector.delete(oc, uid, oo);
			LOG.info("Delete on AD was executed");
			deleteWinrm(oc, uid);
			LOG.info("Delete via WinRM was executed");
		} else if (cmdConfiguration.isDeleteViaAd() && cmdConfiguration.isFirstDeleteWinRM() && cmdConfiguration.isDeleteViaWinRM()) {
			deleteWinrm(oc, uid);
			LOG.info("Delete via WinRM was executed");
			adConnector.delete(oc, uid, oo);
			LOG.info("Delete on AD was executed");
		} else if (cmdConfiguration.isDeleteViaAd() && !cmdConfiguration.isDeleteViaWinRM()) {
			adConnector.delete(oc, uid, oo);
		} else if (!cmdConfiguration.isDeleteViaAd() && cmdConfiguration.isDeleteViaWinRM()) {
			deleteWinrm(oc, uid);
		}
	}

	private void deleteWinrm(ObjectClass oc, Uid uid) {
		Set<Attribute> attributesWithConfig = setConfToAttrs(new HashSet<>());
		attributesWithConfig.add(AttributeBuilder.build(SCRIPT_ATT_NAME, cmdConfiguration.getDeletePsPath()));
		new CmdDelete(oc, cmdConfiguration.getDeleteCmdPath(), uid, attributesWithConfig).execDeleteCmd();
	}

	@Override
	public void test() {
		LOG.ok("Remote connection test");

		if (!cmdConfiguration.isTestViaAd() && !cmdConfiguration.isTestViaWinRM()) {
			throw new ConnectorException("You need to select at least one protocol for test method");
		}

		if (cmdConfiguration.isTestViaAd()) {
			adConnector.test();
		}
		if (cmdConfiguration.isTestViaWinRM()) {
			Set<Attribute> attributesWithConfig = setConfToAttrs(new HashSet<>());
			new CmdTest(cmdConfiguration.getTestCmdPath(), attributesWithConfig).test();
		}
	}

	@Override
	public void executeQuery(
			final ObjectClass oc,
			final LdapFilter operand,
			final ResultsHandler rh,
			final OperationOptions oo) {

		if (LOG.isOk()) {
			LOG.ok("Search parameters:");
			LOG.ok("ObjectClass {0}", oc.getObjectClassValue());
			LOG.ok("Operand {0}", operand);
			if (oo != null) {
				for (Map.Entry<String, Object> entrySet : oo.getOptions().entrySet()) {
					LOG.ok("OperationOptions {0}: {1}", entrySet.getKey(), entrySet.getValue());
				}
			}
		}

		Map<String, ConnectorObject> results = new HashMap<>();
		try {
			if (cmdConfiguration.isSearchViaAd()) {
				adConnector.executeQuery(oc, operand, connectorObject -> {
					if (cmdConfiguration.isUseWinRMForLdapGroups() && oc.equals(ObjectClass.GROUP)) {
						return returnOnlyCorrectValueInMultivalue(oc, operand, results, connectorObject);
					} else {
						results.put(connectorObject.getUid().getUidValue(), connectorObject);
						return true;
					}
				}, oo);
			}
			if (cmdConfiguration.isSearchViaWinRM()) {
				Set<Attribute> attributesWithConfig = setConfToAttrs(new HashSet<>());
				attributesWithConfig.add(AttributeBuilder.build(SCRIPT_ATT_NAME, cmdConfiguration.getSearchPsPath()));

				String query;
				Operand filter = null;
				if (operand != null) {
					query = operand.getNativeFilter().substring(1, operand.getNativeFilter().length() - 1);
					filter = new Operand(Operator.EQ, "__UID__", query.substring(query.indexOf('=') + 1), false);
				}
				new CmdExecuteQuery(oc, cmdConfiguration.getSearchCmdPath(), filter, connectorObject -> {
					String uidValue = connectorObject.getUid().getUidValue();
					if (results.containsKey(uidValue)) {
						Set<Attribute> attributes = new HashSet<>();
						attributes.addAll(results.get(uidValue).getAttributes());
						attributes.addAll(connectorObject.getAttributes());

						ConnectorObject o = new ConnectorObject(ObjectClass.ACCOUNT, attributes);
						results.put(uidValue, o);
						return false;
					}
					results.put(uidValue, connectorObject);
					return false;
				}, attributesWithConfig).execQuery();
			}
		} catch (ConnectException ex) {
			LOG.error("Error in connection process", ex);
			throw new ConnectorException(ex);
		}
		if (!results.isEmpty()) {
			results.forEach((key, value) -> rh.handle(value));
		}
	}

	private boolean returnOnlyCorrectValueInMultivalue(ObjectClass oc, LdapFilter operand, Map<String, ConnectorObject> results, ConnectorObject connectorObject) {
		String uidValue = connectorObject.getUid().getUidValue();
		Set<Attribute> attributes = new HashSet<>(connectorObject.getAttributes());

		Attribute member = attributes.stream().filter(attribute -> attribute.getName().equals("member")).findFirst().orElse(null);

		if (member != null) {
			attributes.remove(member);

			List<Object> valueFromFilter = new ArrayList<>();
			String userDn = operand.getNativeFilter().substring(operand.getNativeFilter().indexOf('=') + 1, operand.getNativeFilter().indexOf(')'));
			valueFromFilter.add(unEscapeStringAttrValue(userDn));

			attributes.add(AttributeBuilder.build(member.getName(), valueFromFilter));
			ConnectorObject o = new ConnectorObject(oc, attributes);
			results.put(uidValue, o);
		}
		return true;
	}

	@Override
	public FilterTranslator<LdapFilter> createFilterTranslator(final ObjectClass oc, final OperationOptions oo) {
		if (oc == null || (!oc.equals(ObjectClass.ACCOUNT)) && (!oc.equals(ObjectClass.GROUP))) {
			throw new IllegalArgumentException("Invalid objectclass");
		}
		return adConnector.createFilterTranslator(oc, oo);
	}

	@Override
	public Schema schema() {

		//TODO add better condition
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			return adConnector.schema();
		}
		ObjectClassInfoBuilder accountObjectClassBuilder = new ObjectClassInfoBuilder();
		accountObjectClassBuilder.setType(ObjectClass.ACCOUNT_NAME);
		accountObjectClassBuilder.addAttributeInfo(AttributeInfoBuilder.build("__UID__", String.class));
		accountObjectClassBuilder.addAttributeInfo(AttributeInfoBuilder.build("__PASSWORD__", GuardedString.class));

		SchemaBuilder schemaBuilder = new SchemaBuilder(CmdConnector.class);
		schemaBuilder.defineObjectClass(accountObjectClassBuilder.build());
		return schemaBuilder.build();
	}

	@Override
	public void sync(ObjectClass objectClass, SyncToken token, SyncResultsHandler handler, OperationOptions options) {
		if (cmdConfiguration.isSearchViaAd()) {
			adConnector.sync(objectClass, token, handler, options);
		} else {
			throw new ConnectorException("Sync is supported only for AD");
		}
	}

	@Override
	public SyncToken getLatestSyncToken(ObjectClass objectClass) {
		if (cmdConfiguration.isSearchViaAd()) {
			return adConnector.getLatestSyncToken(objectClass);
		}
		throw new ConnectorException("Sync is supported only for AD");
	}

	@Override
	public Uid authenticate(ObjectClass objectClass, String username, GuardedString password, OperationOptions options) {
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			return adConnector.authenticate(objectClass, username, password, options);
		}
		throw new ConnectorException("Authenticate is supported only for AD");
	}

	@Override
	public Uid resolveUsername(ObjectClass objectClass, String username, OperationOptions options) {
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			return adConnector.resolveUsername(objectClass, username, options);
		}
		throw new ConnectorException("Resolve user name is supported only for AD");
	}

	@Override
	public void checkAlive() {
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			adConnector.checkAlive();
		} else {
			throw new ConnectorException("Check alive is supported only for AD");
		}
	}

	@Override
	public Uid addAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToAdd, OperationOptions options) {
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			return adConnector.addAttributeValues(objclass, uid, valuesToAdd, options);
		}
		throw new ConnectorException("Add attribute value is supported only for AD");
	}

	@Override
	public Uid removeAttributeValues(ObjectClass objclass, Uid uid, Set<Attribute> valuesToRemove, OperationOptions options) {
		if (cmdConfiguration.isTestViaAd() || cmdConfiguration.isCreateViaAd() || cmdConfiguration.isUpdateViaAd() || cmdConfiguration.isSearchViaAd()) {
			return adConnector.removeAttributeValues(objclass, uid, valuesToRemove, options);
		}
		throw new ConnectorException("Remove attribute value is supported only for AD");
	}

	private Uid checkAndGetUid(Uid uidAd, Uid uidWinrm, String operation) {
		if (uidAd != null && uidWinrm != null) {
			return uidAd;
		} else if (uidAd == null && uidWinrm == null) {
			throw new ConnectorException("You didnt configure any connector for operation: " + operation);
		} else {
			return uidAd == null ? uidWinrm : uidAd;
		}
	}

	/**
	 * Add configuration attributes to set which we send to script
	 *
	 * @param attOriginal
	 * @return
	 */
	private Set<Attribute> setConfToAttrs(Set<Attribute> attOriginal) {
		Set<Attribute> attributes = new HashSet<>();

		// convert null values to empty string otherwise we can't set them to environment
		attOriginal.forEach(attribute -> {
			if (attribute.getValue() == null) {
				attributes.add(AttributeBuilder.build(attribute.getName(), ""));
			} else {
				attributes.add(attribute);
			}
		});
		attributes.add(AttributeBuilder.build("endpoint", cmdConfiguration.getEndpoint()));
		attributes.add(AttributeBuilder.build("authentication", cmdConfiguration.getAuthenticationSchema()));
		attributes.add(AttributeBuilder.build("user", cmdConfiguration.getUser()));
		attributes.add(AttributeBuilder.build("password", getPassword(cmdConfiguration.getPassword())));
		return attributes;
	}

	/**
	 * Get password as plain string
	 *
	 * @param password
	 * @return
	 */
	private String getPassword(GuardedString password) {
		GuardedStringAccessor accessor = new GuardedStringAccessor();
		password.access(accessor);
		char[] result = accessor.getArray();
		return new String(result);
	}


	private String unEscapeStringAttrValue(String string) {
		if (StringUtil.isEmpty(string)) {
			return "";
		} else {
			string = string.replace("\\28", "(");
			string = string.replace("\\29", ")");
			string = string.replace("\\2a", "*");
			string = string.replace("\\5c", "\\");

			return string;
		}
	}
}
