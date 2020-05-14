/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
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
package net.tirasa.connid.bundles.cmd.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.identityconnectors.common.IOUtil;
import org.identityconnectors.common.Pair;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.common.security.GuardedString;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.AttributeUtil;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.OperationalAttributes;
import org.identityconnectors.framework.common.objects.Uid;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.tirasa.connid.bundles.cmd.CmdConfiguration;
import net.tirasa.connid.bundles.cmd.CmdConnection;

public abstract class CmdExec {

	private static final Log LOG = Log.getLog(CmdExec.class);

	protected final ObjectClass oc;

	public CmdExec(final ObjectClass oc) {
		this.oc = oc;
	}

	protected Process exec(final String path, final List<Pair<String, String>> env) {
		try {
			return CmdConnection.openConnection().execute(path, env);
		} catch (Exception e) {
			LOG.error(e, "Error executing script: " + path);
			throw new ConnectorException(e);
		}
	}

	protected List<Pair<String, String>> createEnv(final Set<Attribute> attrs) {
		return createEnv(attrs, null);
	}

	protected List<Pair<String, String>> createEnv(final Set<Attribute> attrs, final Uid uid) {
		final List<Pair<String, String>> env = new ArrayList<Pair<String, String>>();

		LOG.ok("Creating environment with:");
		if (oc != null) {
			LOG.ok(CmdConfiguration.OBJECT_CLASS + ": {0}", oc.getObjectClassValue());
			env.add(new Pair<String, String>(CmdConfiguration.OBJECT_CLASS, oc.getObjectClassValue()));
		}

		for (Attribute attr : attrs) {
			if (attr.getValue() != null && !attr.getValue().isEmpty()) {
				LOG.ok("Environment variable {0}: {1}", attr.getName(), attr.getValue().get(0));

				if (attr.getValue().size() > 1 || attr.getName().equals("roles") || attr.getName().equals("ldapGroups") ||
						attr.getName().equals("ldapGroupsToAdd") || attr.getName().equals("ldapGroupsToRemove")) {
					ObjectMapper objectMapper = new ObjectMapper();
					try {
						String json = objectMapper.writeValueAsString(attr.getValue());
						env.add(new Pair<>(attr.getName(), json));
						continue;
					} catch (JsonProcessingException e) {
						throw new ConnectorException("Cannot parse multivalued attr " + attr.getName() + " to json");
					}
				}

				if (OperationalAttributes.PASSWORD_NAME.equals(attr.getName())) {
					final GuardedString gpasswd = AttributeUtil.getPasswordValue(attrs);
					if (gpasswd != null) {
						gpasswd.access(new GuardedString.Accessor() {

							@Override
							public void access(char[] clearChars) {
								env.add(new Pair<String, String>(
										OperationalAttributes.PASSWORD_NAME, new String(clearChars)));
							}
						});
					}
				} else {
					env.add(new Pair<String, String>(
							attr.getName(), IOUtil.join(attr.getValue().toArray(), ',')));
				}
			}
		}

		if (uid != null && AttributeUtil.find(Uid.NAME, attrs) == null) {
			LOG.ok("Environment variable {0}: {1}", Uid.NAME, uid.getUidValue());
			env.add(new Pair<String, String>(Uid.NAME, uid.getUidValue()));
		}

		return env;
	}

	protected Uid waitFor(final Process proc) {
		try {
			proc.waitFor();

			String output = getInputStream(proc.getInputStream());
			LOG.info("Script output:\n{0}", output);
			String error = getInputStream(proc.getErrorStream());
			LOG.error("Script error:\n{0}", error);

			if (proc.exitValue() != 0) {
				throw new ConnectorException("Script ended with exit code: " + proc.exitValue() + " error: " + error);
			}
			final Properties attrs = StringUtil.toProperties(output);
			LOG.info("Process ended");
			String uid = attrs.getProperty(Uid.NAME);
			if (!StringUtil.isBlank(uid)) {
				return new Uid(uid);
			}
		} catch (InterruptedException e) {
			LOG.error(e, "Error waiting for termination");
		}
		return null;
	}

	private String getInputStream(InputStream stream) {
		String output = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
			if (br.ready()) {
				output = br.lines().collect(Collectors.joining(System.lineSeparator()));
			}
		} catch (IOException e) {
			LOG.warn("Cannot get stdout from script");
		}
		return output;
	}
}
