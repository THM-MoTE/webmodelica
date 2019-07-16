/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica.models.mope

import io.circe.generic.JsonCodec

/** A position (2D Point) inside of a file */
@JsonCodec
case class FilePosition(line: Int, column: Int)
