package com.tvd12.ezyfoxserver.creator;

import com.tvd12.ezyfox.builder.EzyBuilder;
import com.tvd12.ezyfoxserver.entity.EzySession;
import com.tvd12.ezyfoxserver.setting.EzySessionManagementSetting;
import com.tvd12.ezyfoxserver.socket.EzyChannel;
import com.tvd12.ezyfoxserver.wrapper.EzySessionManager;

@SuppressWarnings("rawtypes")
public class EzySimpleSessionCreator implements EzySessionCreator {

    protected final EzySessionManager sessionManager;
    protected final EzySessionManagementSetting sessionSetting;
    
    public EzySimpleSessionCreator(Builder builder) {
        this.sessionManager = builder.sessionManager;
        this.sessionSetting = builder.sessionSetting;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <S extends EzySession> S create(EzyChannel channel) {
        S session = (S) newSession(channel);
        session.setActivated(true);
        session.setMaxIdleTime(sessionSetting.getSessionMaxIdleTime());
        session.setMaxWaitingTime(sessionSetting.getSessionMaxWaitingTime());
        return session;
    }

    protected EzySession newSession(EzyChannel channel) {
        return sessionManager.provideSession(channel);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder implements EzyBuilder<EzySessionCreator> {
        
        private EzySessionManager sessionManager;
        private EzySessionManagementSetting sessionSetting;
        
        public Builder sessionManager(EzySessionManager sessionManager) {
            this.sessionManager = sessionManager;
            return this;
        }
        
        public Builder sessionSetting(EzySessionManagementSetting sessionSetting) {
            this.sessionSetting = sessionSetting;
            return this;
        }

        @Override
        public EzySessionCreator build() {
            return new EzySimpleSessionCreator(this);
        }
        
    }
}
