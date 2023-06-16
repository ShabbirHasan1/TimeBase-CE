/*
 * Copyright 2021 EPAM Systems, Inc
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.deltix.qsrv.util.json;

/**
 * @author Daniil Yarmalkevich
 * Date: 11/6/2019
 */
public class JSONRawMessagePrinterFactory {

    public static JSONRawMessagePrinter createForTimebaseWebGateway() {
        return new JSONRawMessagePrinter(false, true, DataEncoding.STANDARD,
                true, false, PrintType.FULL, true, "$type");
    }

    public static JSONRawMessagePrinter create(String typeField) {
        return new JSONRawMessagePrinter(false, true, DataEncoding.STANDARD, true, false, PrintType.FULL, typeField);
    }

    public static JSONRawMessagePrinter createForTickDBShell() {
        return new JSONRawMessagePrinter(false, true, DataEncoding.STANDARD, true,
            false, PrintType.FULL, true, "$type") {
            @Override
            protected void appendType(StringBuilder sb, String fullType) {
                if (fullType != null && fullType.startsWith("QUERY"))
                    fullType = "";
                super.appendType(sb, fullType);
            }
        };
    }
}