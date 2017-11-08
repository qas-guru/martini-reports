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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import com.google.gson.JsonObject;

public interface State {

	void setStatus(HSSFCell cell, String status);

	void setThemes(HSSFCell cell, Iterable<String> themes);

	void setExecutionTime(HSSFCell cell, long executionTime);

	void addSuite(JsonObject suite);

	void addFeature(JsonObject feature);

	void updateResults();

	void updateSuites(HSSFSheet sheet);

	JsonObject getFeature(JsonObject result);
}
