////////////////////////////////////////////////////////////////////////
//
//     Copyright (c) 2009-2015 Denim Group, Ltd.
//
//     The contents of this file are subject to the Mozilla Public License
//     Version 2.0 (the "License"); you may not use this file except in
//     compliance with the License. You may obtain a copy of the License at
//     http://www.mozilla.org/MPL/
//
//     Software distributed under the License is distributed on an "AS IS"
//     basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
//     License for the specific language governing rights and limitations
//     under the License.
//
//     The Original Code is ThreadFix.
//
//     The Initial Developer of the Original Code is Denim Group, Ltd.
//     Portions created by Denim Group, Ltd. are Copyright (C)
//     Denim Group, Ltd. All Rights Reserved.
//
//     Contributor(s): Denim Group, Ltd.
//
////////////////////////////////////////////////////////////////////////
package com.denimgroup.threadfix.service.translator;

import com.denimgroup.threadfix.data.entities.Finding;
import com.denimgroup.threadfix.data.entities.Scan;
import com.denimgroup.threadfix.framework.engine.ThreadFixInterface;
import com.denimgroup.threadfix.framework.engine.cleaner.PathCleaner;
import com.denimgroup.threadfix.framework.engine.cleaner.PathCleanerFactory;
import com.denimgroup.threadfix.data.enums.FrameworkType;
import com.denimgroup.threadfix.logging.SanitizedLogger;
import javax.annotation.Nonnull;

class NoSourceFindingProcessor implements FindingProcessor {

    private static final SanitizedLogger LOG = new SanitizedLogger(NoSourceFindingProcessor.class);

    private final PathCleaner cleaner;

    public NoSourceFindingProcessor(PathCleaner cleaner) {
        this.cleaner = cleaner;
    }

    public NoSourceFindingProcessor(FrameworkType frameworkType,
                                    Scan scan) {
        this.cleaner = PathCleanerFactory.getPathCleaner(frameworkType,
                ThreadFixInterface.toPartialMappingList(scan));

        LOG.info("NoSourceFindingProcessor with cleaner = " + cleaner);
    }

    @Override
    public void process(@Nonnull Finding finding) {

        if (finding.getIsStatic()) {
            if (finding.getSourceFileLocation() != null) {
                finding.setCalculatedFilePath(cleaner.cleanStaticPath(
                        finding.getSourceFileLocation()));
            }

            if (finding.getCalculatedUrlPath() == null ||
                    finding.getCalculatedUrlPath().equals(finding.getCalculatedFilePath())) {
                finding.setCalculatedUrlPath(cleaner.getDynamicPathFromStaticPath(
                        finding.getCalculatedFilePath()));
            }
        } else {
            if (finding.getSurfaceLocation() != null &&
                    finding.getSurfaceLocation().getPath() != null) {
                finding.setCalculatedUrlPath(cleaner.cleanDynamicPath(
                        finding.getSurfaceLocation().getPath()));
            }

            // TODO create getStaticPathFromDynamic for symmetry's sake
            if (finding.getSourceFileLocation() != null) {
                finding.setCalculatedFilePath(cleaner.cleanStaticPath(
                        finding.getSourceFileLocation()));
            }
        }
    }

    @Override
    public void printStatistics() {

    }

}
