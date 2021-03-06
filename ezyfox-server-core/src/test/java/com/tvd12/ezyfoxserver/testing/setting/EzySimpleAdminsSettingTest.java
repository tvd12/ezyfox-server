package com.tvd12.ezyfoxserver.testing.setting;

import java.util.Arrays;
import java.util.HashMap;

import org.testng.annotations.Test;

import com.tvd12.ezyfoxserver.setting.EzySimpleAdminSetting;
import com.tvd12.ezyfoxserver.setting.EzySimpleAdminsSetting;
import com.tvd12.test.base.BaseTest;

public class EzySimpleAdminsSettingTest extends BaseTest {
    
    @Test
    public void test() {
        EzySimpleAdminsSetting setting = new EzySimpleAdminsSetting();
        setting.setAdminsByName(new HashMap<>());
        setting.setAdminsByAccessToken(new HashMap<>());
        assert setting.getAdmins().size() == 0;
        EzySimpleAdminSetting adminSetting = new EzySimpleAdminSetting();
        adminSetting.setAccessToken("token");
        adminSetting.setUsername("user");
        adminSetting.setPassword("password");
        setting.setItem(adminSetting);
        assert setting.getAdminByName("user") != null;
        assert setting.getAdminByAccessToken("token") != null;
        assert setting.containsAdminByName("user");
        assert setting.containsAdminByAccessToken("token");
        EzySimpleAdminSetting adminSetting1 = new EzySimpleAdminSetting();
        adminSetting.setAccessToken("token1");
        adminSetting.setUsername("user1");
        adminSetting.setPassword("password1");
        setting.setAdmins(Arrays.asList(adminSetting1));
    }

}
