package com.coreoz.ppt;

import java.util.function.Function;

import lombok.Value;

@Value(staticConstructor = "of")
class PptTextMapper {

	private  Object value;
	private  Function<String, Object> argumentToValue;

}
