/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */

package skinsrestorer.shared.storage;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import skinsrestorer.shared.format.Profile;
import skinsrestorer.shared.format.SkinProfile;

public class SkinStorage {
	private static final SkinStorage instance = new SkinStorage();

	public static SkinStorage getInstance() {
		return instance;
	}

	protected static File pluginfolder;

	public static void init(File pluginfolder) {
		SkinStorage.pluginfolder = pluginfolder;
	}

	private final ConcurrentHashMap<String, SkinProfile> skins = new ConcurrentHashMap<>();

	public boolean isSkinDataForced(String name) {
		SkinProfile profile = skins.get(name.toLowerCase());
		if (profile != null && profile.isForced()) {
			return true;
		}
		return false;
	}

	public void removeSkinData(String name) {
		skins.remove(name.toLowerCase());
	}

	public void setSkinData(String name, SkinProfile profile) {
		skins.put(name.toLowerCase(), profile.cloneAsForced());
	}

	public SkinProfile getOrCreateSkinData(String name) {
		return skins.compute(name.toLowerCase(), (playername, profile) -> profile != null ? profile : new SkinProfile(new Profile(null, playername), null, 0, false));
	}
}
