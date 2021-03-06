package com.tvd12.ezyfoxserver.socket;

import static com.tvd12.ezyfox.util.EzyProcessor.processWithLogException;

import com.tvd12.ezyfoxserver.entity.EzySession;

import lombok.Setter;

public class EzySocketWriter 
        extends EzySocketAbstractEventHandler
        implements EzySessionTicketsQueueAware {

    @Setter
	protected EzySessionTicketsQueue sessionTicketsQueue;
    @Setter
    protected EzySocketWriterGroupFetcher writerGroupFetcher;
	
	@Override
    public void handleEvent() {
	    processSessionTicketsQueue0();
	}
	
	@Override
    public void destroy() {
        processWithLogException(() -> sessionTicketsQueue.clear());
    }
	
	private void processSessionTicketsQueue0() {
		try {
			EzySession session = sessionTicketsQueue.take();
			processSessionQueue(session);
		} 
		catch (InterruptedException e) {
			logger.info("socket-writer thread interrupted: {}", Thread.currentThread());
		}
		catch(Throwable throwable) {
			logger.warn("problems in socket-writer, thread: {}", Thread.currentThread(), throwable);
		}
	}
	
	private void processSessionQueue(EzySession session) throws Exception {
	    EzySocketWriterGroup group = getWriterGroup(session);
		if(group == null) 
			return;
		EzyPacketQueue queue = session.getPacketQueue();
		synchronized (queue) {
		    boolean emptyQueue = processSessionQueue(group, queue);
	        if(!emptyQueue) { 
	            sessionTicketsQueue.add(session);
	        }
        }
	}
	
	private boolean processSessionQueue(EzySocketWriterGroup group, EzyPacketQueue queue)
			throws Exception {
		if(!queue.isEmpty()) {
			EzyPacket packet = queue.peek();
			Object writeBuffer = getWriteBuffer();
			group.firePacketSend(packet, writeBuffer);
	        if(packet.isReleased())
	            queue.take();
			return queue.isEmpty();
		}
		return true;
	}
	
	protected Object getWriteBuffer() {
	    return null;
	}
	
	protected EzySocketWriterGroup getWriterGroup(EzySession session) {
        return writerGroupFetcher.getWriterGroup(session);
    }
	
}
