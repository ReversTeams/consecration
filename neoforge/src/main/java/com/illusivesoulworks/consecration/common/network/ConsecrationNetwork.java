/*
 * Copyright (C) 2017-2023 Illusive Soulworks
 *
 * Consecration is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Consecration is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Consecration.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.consecration.common.network;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
public class ConsecrationNetwork {
 public static void register(RegisterPayloadHandlersEvent event) {
  event.registrar("1").playToClient(SPacketSetVulnerability.TYPE, SPacketSetVulnerability.STREAM_CODEC,
      (msg, context) -> SPacketSetVulnerability.handle(msg));
 }
}
