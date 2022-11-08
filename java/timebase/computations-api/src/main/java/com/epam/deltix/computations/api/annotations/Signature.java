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


import java.lang.annotation.*;

/**
 * Information about a plug-in function.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Repeatable(MultiSignatureFunction.class)
public @interface Signature {
    /**
     * Id in QQL.
     */
    String id();

    /**
     * Argument types (add "?" if nullable). Example: <code>INTEGER?</code>
     */
    String[] args();

    /**
     * Return types (add "?" if nullable). Example: <code>INTEGER?</code>
     */
    String returns();
}