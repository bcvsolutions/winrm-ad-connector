/**
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
package net.tirasa.connid.bundles.cmd.dto;

import java.util.List;
import java.util.Map;

/**
 * @author Roman Kučera
 */
public class SearchResponse {
	public List<Map<String, String>> users;

	public List<Map<String, String>> getUsers() {
		return users;
	}

	public void setUsers(List<Map<String, String>> users) {
		this.users = users;
	}
}