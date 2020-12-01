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
package net.tirasa.connid.bundles.cmd.methods;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import org.identityconnectors.common.Pair;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.ConnectorException;
import org.identityconnectors.framework.common.objects.Attribute;
import org.identityconnectors.framework.common.objects.ConnectorObjectBuilder;
import org.identityconnectors.framework.common.objects.Name;
import org.identityconnectors.framework.common.objects.ObjectClass;
import org.identityconnectors.framework.common.objects.ResultsHandler;
import org.identityconnectors.framework.common.objects.Uid;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.tirasa.connid.bundles.cmd.dto.SearchResponse;
import net.tirasa.connid.bundles.cmd.search.Operand;

public class CmdExecuteQuery extends CmdExec {

	private static final Log LOG = Log.getLog(CmdExecuteQuery.class);

	private static final String ITEM_SEPARATOR = "--- NEW SEARCH RESULT ITEM ---";

	private final String scriptPath;

	private final Operand filter;

	private final ResultsHandler resultsHandler;

	private final Set<Attribute> attrs;

	public CmdExecuteQuery(final ObjectClass oc, final String scriptPath, final Operand filter, final ResultsHandler rh, Set<Attribute> attrs) {
		super(oc);

		this.scriptPath = scriptPath;
		this.filter = filter;
		this.resultsHandler = rh;
		this.attrs = attrs;
	}

	public void execQuery() throws ConnectException {
		final Process proc;

		List<Pair<String, String>> env = createEnv(attrs);

		if (filter == null) {
			LOG.ok("Full search (no filter) ...");
			proc = exec(scriptPath, env);
			readOutput(proc);
		} else {
			LOG.ok("Search with filter {0} ...", filter);
			env.add(new Pair<String, String>(filter.getAttributeName(), filter.getAttributeValue()));
			proc = exec(scriptPath, env);
			switch (filter.getOperator()) {
				case EQ:
					readOutput(proc);
					break;
				case SW:
					break;
				case EW:
					break;
				case C:
					break;
				case OR:
					break;
				case AND:
					break;
				default:
					throw new ConnectorException("Wrong Operator");
			}
		}

		waitFor(proc);
	}

	private void readOutput(final Process proc) throws ConnectException {
		LOG.info("Read for script output ...");

		final BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8));
		final StringBuilder buffer = new StringBuilder();

		StringBuilder lines = new StringBuilder();
		String line;

		try {
			while ((line = br.readLine()) != null) {
				lines.append(line);
				lines.append("\n");
			}
		} catch (IOException e) {
			LOG.error(e, "Error reading result items");
		}

		try {
			br.close();
		} catch (IOException e) {
			LOG.ok(e, "Error closing reader");
		}

		LOG.info("Script output: {0}", lines.toString());

		String json = "{\n\"users\": ";

		int startArray = lines.indexOf("[");
		int endArray = lines.indexOf("]");
		if (startArray > 0 && endArray > 0) {
			json += lines.toString().substring(startArray,  endArray + 1);
			json += "\n}";

			LOG.info("JSON: {0}", json);
			LOG.info("END OF JSON");
			handleJsonResponse(json);
		} else {
			LOG.info("No json response: {0}", lines.toString());
		}
	}

	private void handleJsonResponse(final String json) throws ConnectException {
		if (json == null || json.isEmpty()) {
			throw new ConnectException("No results found");
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			SearchResponse users = mapper.readValue(json, SearchResponse.class);
			users.getUsers().forEach(map -> {
				ConnectorObjectBuilder bld = new ConnectorObjectBuilder();
				// If uid is empty returning nothing
				if (map.containsKey(Uid.NAME) && !map.get(Uid.NAME).isEmpty()) {
					map.forEach((key, value) -> {
						if (key.equals(Uid.NAME)) {
							bld.setUid(value);
						} else if (key.equals(Name.NAME)) {
							bld.setName(map.get("__NAME__"));
						} else {
							bld.addAttribute(key, value);
						}
					});
					bld.setObjectClass(oc);
					resultsHandler.handle(bld.build());
				}
			});
		} catch (IOException e) {
			throw new ConnectException("Can not parse json " + e);
		}
	}
}
