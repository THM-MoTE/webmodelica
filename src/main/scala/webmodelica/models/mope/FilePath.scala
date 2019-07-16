/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models.mope

import io.circe.generic.JsonCodec

/** Wrapper around a path to a file. */
@JsonCodec
case class FilePath(path: String)
