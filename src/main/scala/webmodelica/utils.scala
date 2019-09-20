/*
 * Copyright (c) 2019-Today N. Justus
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package webmodelica

object utils {
  /** Returns only every n-th element of the given seq.
    * source: https://stackoverflow.com/questions/25227475/list-of-every-n-th-item-in-a-given-list
    */
  def skip[A](l:Seq[A], n:Int) =
    l.zipWithIndex.collect {case (e,i) if ((i+1) % n) == 0 => e}
}
