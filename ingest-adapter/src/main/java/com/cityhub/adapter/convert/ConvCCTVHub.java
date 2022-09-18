/**
 *
 * Copyright 2021 PINE C&I CO., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cityhub.adapter.convert;

import java.util.Iterator;

import org.json.JSONObject;

import com.cityhub.core.AbstractConvert;
import com.cityhub.exception.CoreException;
import com.cityhub.utils.DataCoreCode.ErrorCode;
import com.cityhub.utils.DataCoreCode.SocketCode;

public class ConvCCTVHub extends AbstractConvert {

	@Override
	public void init(JSONObject ConfItem, JSONObject templateItem) {
		super.setup(ConfItem, templateItem);
	}

	@Override
	public String doit() throws CoreException {
		StringBuffer sendJson = new StringBuffer();

		String model_id = ConfItem.get("model_id").toString();
		String template = templateItem.get(model_id).toString();
		Iterator<String> cctvTypeIter = new JSONObject(ConfItem.get("cctvs").toString()).keys();


		try {

			while(cctvTypeIter.hasNext()) {

				String cctvType = cctvTypeIter.next();
				String fullClassName = this.getClass().getName().toString();
				String extClassName = fullClassName.substring(0, fullClassName.lastIndexOf(".")) + ".ConvCCTV" + cctvType.replace(" ", "");

				Class<?> extClass = Class.forName(extClassName);
				ConvCCTV2 convClass = (ConvCCTV2) extClass.newInstance();
				convClass.init(ConfItem, template, cctvType);

				String resultData = convClass.getResult();

				sendJson.append(resultData);
			}

		} catch (CoreException e) {
			if ("!C0099".equals(e.getErrorCode())) {
				log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			}
		} catch (Exception e) {
			log(SocketCode.DATA_CONVERT_FAIL, e.getMessage(), id);
			throw new CoreException(ErrorCode.NORMAL_ERROR, e.getMessage() + "`" + id, e);
		}

		return sendJson.toString();
	} // end of doit
}
// end of class