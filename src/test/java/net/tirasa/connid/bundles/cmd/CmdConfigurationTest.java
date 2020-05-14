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
package net.tirasa.connid.bundles.cmd;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.identityconnectors.common.security.GuardedString;
import org.junit.Assert;
import org.junit.Test;

public class CmdConfigurationTest extends AbstractTest {

	/**
	 * Tests empty configuration
	 */
	@Test
	public final void testValidate() {
		final CmdConfiguration config = new CmdConfiguration();
		try {
			config.validate();
			assertTrue(true);
		} catch (RuntimeException e) {
			Assert.assertNull(e);
		}
	}

	/**
	 * Test ad operation without config
	 */
	@Test
	public final void testAdWithoutConfig() {
		final CmdConfiguration config = new CmdConfiguration();
		config.setTestViaAd(true);
		config.setSearchViaAd(true);
		config.setCreateViaAd(true);
		config.setDeleteViaAd(true);
		config.setUpdateViaAd(true);
		try {
			config.validate();
			fail();
		} catch (RuntimeException e) {
			assertNotNull(e);
		}
	}

	/**
	 * Test ad operation with config
	 */
	@Test
	public final void testAdWithConfig() {
		final CmdConfiguration config = new CmdConfiguration();
		config.setTestViaAd(true);
		config.setSearchViaAd(true);
		config.setCreateViaAd(true);
		config.setDeleteViaAd(true);
		config.setUpdateViaAd(true);
		config.setPrincipal("User");
		config.setCredentials(new GuardedString());
		try {
			config.validate();
			assertTrue(true);
		} catch (RuntimeException e) {
			Assert.assertNull(e);
		}
	}

	/**
	 * Test winrm operation without config
	 */
	@Test
	public final void testWinRmWithoutconfig() {
		final CmdConfiguration config = new CmdConfiguration();
		config.setTestViaWinRM(true);
		config.setSearchViaWinRM(true);
		config.setCreateViaWinRM(true);
		config.setDeleteViaWinRM(true);
		config.setUpdateViaWinRM(true);
		try {
			config.validate();
			fail();
		} catch (RuntimeException e) {
			assertNotNull(e);
		}
	}

	/**
	 * Test winrm operation with config
	 */
	@Test
	public final void testWinRmWithconfig() {
		final CmdConfiguration config = new CmdConfiguration();
		config.setTestViaWinRM(true);
		config.setSearchViaWinRM(true);
		config.setCreateViaWinRM(true);
		config.setDeleteViaWinRM(true);
		config.setUpdateViaWinRM(true);
		config.setCreateCmdPath("path");
		config.setCreatePsPath("path");
		config.setUpdateCmdPath("path");
		config.setUpdatePsPath("path");
		config.setSearchCmdPath("path");
		config.setSearchPsPath("path");
		config.setDeleteCmdPath("path");
		config.setDeletePsPath("path");
		config.setEndpoint("endpoint");
		config.setAuthenticationSchema("credssp");
		config.setUser("user");
		config.setPassword(new GuardedString());
		config.setTestCmdPath("path");

		try {
			config.validate();
			assertTrue(true);
		} catch (RuntimeException e) {
			Assert.assertNull(e);
		}
	}
}
