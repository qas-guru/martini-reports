/*
Copyright 2017 Penny Rohr Curich

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package guru.qas.martini.report;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import static com.google.common.base.Preconditions.checkNotNull;

public enum JsonObjectType {
	SUITE("suite"),
	FEATURE("feature"),
	RESULT("martini");

	private final String key;

	JsonObjectType(String key) {
		this.key = key;
	}

	public boolean is(JsonObject object) {
		JsonElement element = object.get(key);
		return null != element && element.isJsonObject();
	}

	public JsonObject get(JsonObject container) {
		return container.getAsJsonObject(key);
	}

	public static JsonObjectType evaluate(JsonObject object) {
		checkNotNull(object, "null JsonObject");

		JsonObjectType type = null;
		if (SUITE.is(object)) {
			type = SUITE;
		}
		else if (FEATURE.is(object)) {
			type = FEATURE;
		}
		else if (RESULT.is(object)) {
			type = RESULT;
		}
		return type;
	}
}
