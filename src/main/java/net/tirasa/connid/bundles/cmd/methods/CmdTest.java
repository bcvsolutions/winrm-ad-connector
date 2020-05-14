/**
 * Copyright (C) 2011 ConnId (connid-dev@googlegroups.com)
 * Copyright (C) 2020 BCV solutions s.r.o., Czech Republic (http://www.bcvsolutions.eu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.tirasa.connid.bundles.cmd.methods;

import java.util.Set;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.Attribute;

public class CmdTest extends CmdExec {

    private static final Log LOG = Log.getLog(CmdTest.class);

    private final String scriptPath;
    private final Set<Attribute> attrs;

    public CmdTest(final String scriptPath, final Set<Attribute> attrs) {
        super(null);

        this.scriptPath = scriptPath;
        this.attrs = attrs;
    }

    public final void test() {
        LOG.info("Executing test on {0}", scriptPath);
        
        waitFor(exec(scriptPath, createEnv(attrs)));
    }
}
