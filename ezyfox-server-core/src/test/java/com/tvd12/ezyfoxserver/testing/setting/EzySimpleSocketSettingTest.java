package com.tvd12.ezyfoxserver.testing.setting;

import org.testng.annotations.Test;

import com.tvd12.ezyfoxserver.setting.EzySimpleSocketSetting;
import com.tvd12.test.base.BaseTest;

public class EzySimpleSocketSettingTest extends BaseTest {

    @Test
    public void test() {
        EzySimpleSocketSetting setting = new EzySimpleSocketSetting();
        setting.setPort(123);
        assert setting.getPort() == 123;
        setting.setAddress("127.0.0.1");
        assert setting.getAddress().equals("127.0.0.1");
        setting.setActive(true);
        assert setting.isActive();
        setting.setCodecCreator("hello");
        assert setting.getCodecCreator().equals("hello");
    }
    
}
