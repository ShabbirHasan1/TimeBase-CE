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
package com.epam.deltix.qsrv.test.messages;

import com.epam.deltix.timebase.messages.MessageInfo;
import com.epam.deltix.util.collections.generated.LongList;

/**
 */
public interface DecimalListMessageInfo extends MessageInfo {
  /**
   * @return Decimal List
   */
  LongList getDecimalList();

  /**
   * @return true if Decimal List is not null
   */
  boolean hasDecimalList();

  /**
   * @return Decimal Nullable List
   */
  LongList getDecimalNullableList();

  /**
   * @return true if Decimal Nullable List is not null
   */
  boolean hasDecimalNullableList();

  /**
   * Method copies state to a given instance
   */
  @Override
  DecimalListMessageInfo clone();
}
