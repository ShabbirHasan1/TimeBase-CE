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
package com.epam.deltix.util.security;

import com.epam.deltix.qsrv.hf.pub.RawMessage;
import com.epam.deltix.util.lang.Disposable;

import java.security.Principal;

/**
 * Created by Alex Karpovich on 04/11/2019.
 */
public interface TimebaseAccessController  extends Disposable {

    //boolean                     connected(Principal user, String address);

    DataFilter<RawMessage> createFilter(Principal user, String address);

    //void                        disconnected(Principal user, String address);
}