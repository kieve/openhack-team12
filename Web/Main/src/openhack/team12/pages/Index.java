package openhack.team12.pages;

import java.util.Map;

class Index extends Page {
	Index() {}

	@Override
	String getTitle() {
		return "Welcome to Team 12 Minecraft";
	}

	@Override
	void process(Map<String, Object> templateInput) {
		templateInput.put("testParam", "Hello World");
	}
}
