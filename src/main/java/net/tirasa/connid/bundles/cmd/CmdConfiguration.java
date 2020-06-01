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

import java.util.Arrays;
import java.util.List;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;

import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConfigurationException;
import org.identityconnectors.framework.spi.ConfigurationProperty;

import net.tirasa.connid.bundles.ad.ADConfiguration;

public class CmdConfiguration extends ADConfiguration {

	public static List<String> SUPPORTED_AUTHENTICATION_SCHEMES = Arrays.asList("basic", "kerberos", "ntlm", "credssp");
	public static final String OBJECT_CLASS = "OBJECT_CLASS";

	private String createCmdPath;
	private String createPsPath;
	private String updateCmdPath;
	private String updatePsPath;
	private String searchCmdPath;
	private String searchPsPath;
	private String deleteCmdPath;
	private String deletePsPath;
	private String testCmdPath;
	private String endpoint;
	private String authenticationSchema;
	private String user;
	private GuardedString password;
	//	Config which protocol will be used
	private boolean createViaAd;
	private boolean createViaWinRM;
	private boolean updateViaAd;
	private boolean updateViaWinRM;
	private boolean deleteViaAd;
	private boolean deleteViaWinRM;
	private boolean searchViaAd;
	private boolean searchViaWinRM;
	private boolean testViaAd;
	private boolean testViaWinRM;
	// Config which protocol will be first
	private boolean firstCreateWinRM;
	private boolean firstUpdateWinRM;
	private boolean firstDeleteWinRM;
	// Additional config
	private boolean useWinRMForLdapGroups;
	private GuardedString[] additionalCreds;

	@ConfigurationProperty(displayMessageKey = "cmd.createCmdPath.display",
			helpMessageKey = "cmd.createCmdPath.help", order = 1000)
	public String getCreateCmdPath() {
		return createCmdPath;
	}

	public void setCreateCmdPath(String createCmdPath) {
		this.createCmdPath = createCmdPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.createPsPath.display",
			helpMessageKey = "cmd.createPsPath.help", order = 1001)
	public String getCreatePsPath() {
		return createPsPath;
	}

	public void setCreatePsPath(String createPsPath) {
		this.createPsPath = createPsPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.updateCmdPath.display",
			helpMessageKey = "cmd.updateCmdPath.help", order = 1002)
	public String getUpdateCmdPath() {
		return updateCmdPath;
	}

	public void setUpdateCmdPath(String updateCmdPath) {
		this.updateCmdPath = updateCmdPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.updatePsPath.display",
			helpMessageKey = "cmd.updatePsPath.help", order = 1003)
	public String getUpdatePsPath() {
		return updatePsPath;
	}

	public void setUpdatePsPath(String updatePsPath) {
		this.updatePsPath = updatePsPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.searchCmdPath.display",
			helpMessageKey = "cmd.searchCmdPath.help", order = 1004)
	public String getSearchCmdPath() {
		return searchCmdPath;
	}

	public void setSearchCmdPath(String searchCmdPath) {
		this.searchCmdPath = searchCmdPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.searchPsPath.display",
			helpMessageKey = "cmd.searchPsPath.help", order = 1005)
	public String getSearchPsPath() {
		return searchPsPath;
	}

	public void setSearchPsPath(String searchPsPath) {
		this.searchPsPath = searchPsPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.deleteCmdPath.display",
			helpMessageKey = "cmd.deleteCmdPath.help", order = 1006)
	public String getDeleteCmdPath() {
		return deleteCmdPath;
	}

	public void setDeleteCmdPath(String deleteCmdPath) {
		this.deleteCmdPath = deleteCmdPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.deletePsPath.display",
			helpMessageKey = "cmd.deletePsPath.help", order = 1007)
	public String getDeletePsPath() {
		return deletePsPath;
	}

	public void setDeletePsPath(String deletePsPath) {
		this.deletePsPath = deletePsPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.testCmdPath.display",
			helpMessageKey = "cmd.testCmdPath.help", order = 1008)
	public String getTestCmdPath() {
		return testCmdPath;
	}

	public void setTestCmdPath(String testCmdPath) {
		this.testCmdPath = testCmdPath;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.endpoint.display",
			helpMessageKey = "cmd.endpoint.help", order = 1009)
	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.authentication.display",
			helpMessageKey = "cmd.authentication.help", order = 1010)
	public String getAuthenticationSchema() {
		return authenticationSchema;
	}

	public void setAuthenticationSchema(String authenticationSchema) {
		this.authenticationSchema = authenticationSchema;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.user.display",
			helpMessageKey = "cmd.user.help", order = 1011)
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	@ConfigurationProperty(displayMessageKey = "cmd.password.display",
			helpMessageKey = "cmd.password.help", order = 1012, confidential = true)
	public GuardedString getPassword() {
		return password;
	}

	public void setPassword(GuardedString password) {
		this.password = password;
	}

	//	CONFIG properties
	@ConfigurationProperty(displayMessageKey = "config.create.ad.display",
			helpMessageKey = "config.create.ad.help", order = 1012)
	public boolean isCreateViaAd() {
		return createViaAd;
	}

	public void setCreateViaAd(boolean createViaAd) {
		this.createViaAd = createViaAd;
	}

	@ConfigurationProperty(displayMessageKey = "config.create.winrm.display",
			helpMessageKey = "config.create.winrm.help", order = 1013)
	public boolean isCreateViaWinRM() {
		return createViaWinRM;
	}

	public void setCreateViaWinRM(boolean createViaWinRM) {
		this.createViaWinRM = createViaWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.update.ad.display",
			helpMessageKey = "config.update.ad.help", order = 1014)
	public boolean isUpdateViaAd() {
		return updateViaAd;
	}

	public void setUpdateViaAd(boolean updateViaAd) {
		this.updateViaAd = updateViaAd;
	}

	@ConfigurationProperty(displayMessageKey = "config.update.winrm.display",
			helpMessageKey = "config.update.winrm.help", order = 1015)
	public boolean isUpdateViaWinRM() {
		return updateViaWinRM;
	}

	public void setUpdateViaWinRM(boolean updateViaWinRM) {
		this.updateViaWinRM = updateViaWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.delete.ad.display",
			helpMessageKey = "config.delete.ad.help", order = 1016)
	public boolean isDeleteViaAd() {
		return deleteViaAd;
	}

	public void setDeleteViaAd(boolean deleteViaAd) {
		this.deleteViaAd = deleteViaAd;
	}

	@ConfigurationProperty(displayMessageKey = "config.delete.winrm.display",
			helpMessageKey = "config.delete.winrm.help", order = 1017)
	public boolean isDeleteViaWinRM() {
		return deleteViaWinRM;
	}

	public void setDeleteViaWinRM(boolean deleteViaWinRM) {
		this.deleteViaWinRM = deleteViaWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.search.ad.display",
			helpMessageKey = "config.search.ad.help", order = 1018)
	public boolean isSearchViaAd() {
		return searchViaAd;
	}

	public void setSearchViaAd(boolean searchViaAd) {
		this.searchViaAd = searchViaAd;
	}

	@ConfigurationProperty(displayMessageKey = "config.search.winrm.display",
			helpMessageKey = "config.search.winrm.help", order = 1019)
	public boolean isSearchViaWinRM() {
		return searchViaWinRM;
	}

	public void setSearchViaWinRM(boolean searchViaWinRM) {
		this.searchViaWinRM = searchViaWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.test.ad.display",
			helpMessageKey = "config.test.ad.help", order = 1020)
	public boolean isTestViaAd() {
		return testViaAd;
	}

	public void setTestViaAd(boolean testViaAd) {
		this.testViaAd = testViaAd;
	}

	@ConfigurationProperty(displayMessageKey = "config.test.winrm.display",
			helpMessageKey = "config.test.winrm.help", order = 1021)
	public boolean isTestViaWinRM() {
		return testViaWinRM;
	}

	public void setTestViaWinRM(boolean testViaWinRM) {
		this.testViaWinRM = testViaWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.create.first.display",
			helpMessageKey = "config.create.first.help", order = 1022)
	public boolean isFirstCreateWinRM() {
		return firstCreateWinRM;
	}

	public void setFirstCreateWinRM(boolean firstCreateWinRM) {
		this.firstCreateWinRM = firstCreateWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.update.first.display",
			helpMessageKey = "config.update.first.help", order = 1023)
	public boolean isFirstUpdateWinRM() {
		return firstUpdateWinRM;
	}

	public void setFirstUpdateWinRM(boolean firstUpdateWinRM) {
		this.firstUpdateWinRM = firstUpdateWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.delete.first.display",
			helpMessageKey = "config.delete.first.help", order = 1024)
	public boolean isFirstDeleteWinRM() {
		return firstDeleteWinRM;
	}

	public void setFirstDeleteWinRM(boolean firstDeleteWinRM) {
		this.firstDeleteWinRM = firstDeleteWinRM;
	}

	@ConfigurationProperty(displayMessageKey = "config.use.winrm.ldapgroups.display",
			helpMessageKey = "config.use.winrm.ldapgroups.help", order = 1025)
	public boolean isUseWinRMForLdapGroups() {
		return useWinRMForLdapGroups;
	}

	public void setUseWinRMForLdapGroups(boolean useWinRMForLdapGroups) {
		this.useWinRMForLdapGroups = useWinRMForLdapGroups;
	}

	@ConfigurationProperty(displayMessageKey = "config.additionalCreds.display",
			helpMessageKey = "config.additionalCreds.help", order = 1026)
	public GuardedString[] getAdditionalCreds() {
		if (additionalCreds == null) {
			return new GuardedString[0];
		}
		return additionalCreds;
	}

	public void setAdditionalCreds(GuardedString[] additionalCreds) {
		this.additionalCreds = additionalCreds;
	}

	// Override getters from AD/LDAP connector to make them not required

	@ConfigurationProperty(order = 6, required = false,
			displayMessageKey = "baseContextsToSynchronize.display",
			helpMessageKey = "baseContextsToSynchronize.help")
	@Override
	public String[] getBaseContextsToSynchronize() {
		return super.getBaseContextsToSynchronize();
	}

	@ConfigurationProperty(order = 1, required = false,
			displayMessageKey = "host.display",	helpMessageKey = "host.help")
	@Override
	public String getHost() {
		// Host is inherit from Ldap connector, and this field is validate in Ldap validate method. So we send fake value to
		// make this field not required. Without this condition it will failed when you click save.
		if(super.getHost() == null || super.getHost().isEmpty()) {
			return "localhost";
		}
		return super.getHost();
	}

	@Override
	public void validate() {
		if (isCreateViaWinRM() || isUpdateViaWinRM() || isDeleteViaWinRM() || isSearchViaWinRM() || isTestViaWinRM()) {
			if (createViaWinRM && StringUtil.isBlank(createCmdPath)) {
				throw new ConfigurationException("Create path must not be blank!");
			}
			if (createViaWinRM && StringUtil.isBlank(createPsPath)) {
				throw new ConfigurationException("Create PS path must not be blank!");
			}
			if (updateViaWinRM && StringUtil.isBlank(updateCmdPath)) {
				throw new ConfigurationException("Update path must not be blank!");
			}
			if (updateViaWinRM && StringUtil.isBlank(updatePsPath)) {
				throw new ConfigurationException("Update PS path must not be blank!");
			}
			if (searchViaWinRM && StringUtil.isBlank(searchCmdPath)) {
				throw new ConfigurationException("Search path must not be blank!");
			}
			if (searchViaWinRM && StringUtil.isBlank(searchPsPath)) {
				throw new ConfigurationException("Search PS path must not be blank!");
			}
			if (deleteViaWinRM && StringUtil.isBlank(deleteCmdPath)) {
				throw new ConfigurationException("Delete path must not be blank!");
			}
			if (deleteViaWinRM && StringUtil.isBlank(deletePsPath)) {
				throw new ConfigurationException("Delete PS path must not be blank!");
			}
			if (testViaWinRM && StringUtil.isBlank(testCmdPath)) {
				throw new ConfigurationException("Test path must not be blank!");
			}
			if (StringUtil.isBlank(endpoint)) {
				throw new ConfigurationException("Endpoint must not be blank!");
			}
			if (!SUPPORTED_AUTHENTICATION_SCHEMES.contains(authenticationSchema)) {
				throw new ConfigurationException("Unsupported authentication scheme. Use one of " + SUPPORTED_AUTHENTICATION_SCHEMES);
			}
			if (StringUtil.isBlank(user)) {
				throw new ConfigurationException("User must not be blank!");
			}
			if (getPassword() == null) {
				throw new ConfigurationException("Password must not be blank!");
			}
		}
		if (isCreateViaAd() || isUpdateViaAd() || isDeleteViaAd() || isSearchViaAd() || isTestViaAd()) {
			if (StringUtil.isBlank(getHost())) {
				throw new ConfigurationException("Host must not be blank!");
			}
			if (getBaseContextsToSynchronize() != null) {
				checkNoBlankValues(getBaseContextsToSynchronize(), "baseContextsToSynchronize.noBlankValues");
				checkNoInvalidLdapNames(getBaseContextsToSynchronize(), "baseContextsToSynchronize.noInvalidLdapNames");
			}
			if (StringUtil.isBlank(getPrincipal())) {
				throw new ConfigurationException("Principal must not be blank!");
			}
			if (getCredentials() == null) {
				throw new ConfigurationException("Password for principal must not be blank!");
			}
		}
	}

	public String getMessage(String key) {
		return getConnectorMessages().format(key, key);
	}

	private void checkNoBlankValues(String[] array, String errorMessage) {
		String[] arr$ = array;
		int len$ = array.length;

		for(int i$ = 0; i$ < len$; ++i$) {
			String each = arr$[i$];
			if (StringUtil.isBlank(each)) {
				throw new ConfigurationException("The list of base contexts to synchronize cannot contain blank values");
			}
		}
	}

	private void checkNoInvalidLdapNames(String[] array, String errorMessage) {
		String[] arr$ = array;
		int len$ = array.length;

		for(int i$ = 0; i$ < len$; ++i$) {
			String each = arr$[i$];

			try {
				new LdapName(each);
			} catch (InvalidNameException var8) {
				throw new ConfigurationException("The base context to synchronize " + each + " cannot be parsed", var8);
			}
		}
	}
}
