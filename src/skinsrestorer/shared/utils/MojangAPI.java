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

package skinsrestorer.shared.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import skinsrestorer.libs.org.json.simple.JSONArray;
import skinsrestorer.libs.org.json.simple.JSONObject;
import skinsrestorer.libs.org.json.simple.parser.JSONParser;
import skinsrestorer.libs.org.json.simple.parser.ParseException;
import skinsrestorer.shared.format.Profile;
import skinsrestorer.shared.format.SkinProfile;
import skinsrestorer.shared.format.SkinProperty;
import skinsrestorer.shared.utils.SkinFetchUtils.SkinFetchFailedException;
import skinsrestorer.shared.utils.apacheutils.IOUtils;

public class MojangAPI {

	private static final String profileurl = "http://mc.vimeworld.ru/session/uuid.php";
	public static Profile getProfile(String nick) throws SkinFetchFailedException, IOException, ParseException {
		//open connection
		HttpURLConnection connection = (HttpURLConnection) setupConnection(new URL(profileurl));
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		//write body
		DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
		writer.write(JSONArray.toJSONString(Arrays.asList(nick)).getBytes(StandardCharsets.UTF_8));
		writer.flush();
		writer.close();
		//check response code
		if (connection.getResponseCode() == 429) {
			throw new SkinFetchFailedException(SkinFetchFailedException.Reason.RATE_LIMITED);
		}
		//read response
		InputStream is = connection.getInputStream();
		String result = IOUtils.toString(is, StandardCharsets.UTF_8);
		IOUtils.closeQuietly(is);
		JSONArray jsonProfiles = (JSONArray) new JSONParser().parse(result);
		if (jsonProfiles.size() > 0) {
			JSONObject jsonProfile = (JSONObject) jsonProfiles.get(0);
			return new Profile((String) jsonProfile.get("id"), (String) jsonProfile.get("name"));
		}
		throw new SkinFetchFailedException(SkinFetchFailedException.Reason.NO_PREMIUM_PLAYER);
	}

	private static final String skullbloburl = "http://mc.vimeworld.ru/session/profile.php?uuid=";
	public static SkinProfile getSkinProfile(String id) throws IOException, ParseException, SkinFetchFailedException {
		//open connection
		HttpURLConnection connection =  (HttpURLConnection) setupConnection(new URL(skullbloburl+id.replace("-", "")));
		//check response code
		if (connection.getResponseCode() == 429) {
			throw new SkinFetchFailedException(SkinFetchFailedException.Reason.RATE_LIMITED);
		}
		//read response
		InputStream is = connection.getInputStream();
		String result = IOUtils.toString(is, StandardCharsets.UTF_8);
		IOUtils.closeQuietly(is);
		JSONObject obj = (JSONObject) new JSONParser().parse(result);
		String username = (String) obj.get("name");
		JSONArray properties = (JSONArray) (obj).get("properties");
		for (int i = 0; i < properties.size(); i++) {
			JSONObject property = (JSONObject) properties.get(i);
			String name = (String) property.get("name");
			String value = (String) property.get("value");
			String signature = (String) property.get("signature");
			if (name.equals("textures")) {
				return new SkinProfile(new Profile(id, username), new SkinProperty(name, value, signature), System.currentTimeMillis(), false);
			}
		}
		throw new SkinFetchFailedException(SkinFetchFailedException.Reason.NO_SKIN_DATA);
	}

	private static URLConnection setupConnection(URL url) throws IOException {
		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(10000);
		connection.setReadTimeout(10000);
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}

}
