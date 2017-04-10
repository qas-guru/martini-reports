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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Joiner;
import com.google.common.io.LineProcessor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

@SuppressWarnings("WeakerAccess")
public class MartiniProcessor implements LineProcessor<JsonObject> {
	protected static final String MARTINI_KEY = "martini";

	protected int openingBracketCount;
	protected int closingBracketCount;
	protected final List<String> lines;

	protected MartiniProcessor() {
		openingBracketCount = 0;
		closingBracketCount = 0;
		lines = new ArrayList<>();
	}

	@Override
	public boolean processLine(String s) throws IOException {
		openingBracketCount += s.chars().filter(e -> e == '{').count();
		closingBracketCount += s.chars().filter(e -> e == '}').count();
		lines.add(s.trim());
		return openingBracketCount != closingBracketCount;
	}

	@Override
	public JsonObject getResult() {
		String joined = Joiner.on('\n').skipNulls().join(lines);
		Gson gson = new GsonBuilder().setLenient().serializeNulls().create();
		JsonObject jsonObject = gson.fromJson(joined, JsonObject.class);
		return jsonObject.getAsJsonObject(MARTINI_KEY);
	}
}
