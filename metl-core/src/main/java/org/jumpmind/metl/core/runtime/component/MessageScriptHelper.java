package org.jumpmind.metl.core.runtime.component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.jumpmind.db.sql.Row;
import org.jumpmind.metl.core.model.Flow;
import org.jumpmind.metl.core.model.FlowStep;
import org.jumpmind.metl.core.model.Model;
import org.jumpmind.metl.core.runtime.EntityData;
import org.jumpmind.metl.core.runtime.LogLevel;
import org.jumpmind.metl.core.runtime.Message;
import org.jumpmind.metl.core.runtime.flow.IMessageTarget;
import org.jumpmind.metl.core.runtime.resource.IResourceRuntime;
import org.jumpmind.metl.core.util.ComponentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class MessageScriptHelper {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
	protected ComponentContext context;
	
	protected FlowStep flowStep;
	
	protected Flow flow;

    protected Iterator<EntityData> entityDataIterator;

    protected Message inputMessage;

    protected IMessageTarget messageTarget;
    
    protected ComponentStatistics componentStatistics;
    
    protected IResourceRuntime resource;

    public MessageScriptHelper(IComponentRuntime component) {
        this.context = component.getComponentContext();
        this.resource = context.getResourceRuntime();
        this.componentStatistics = context.getComponentStatistics();
        this.flow = context.getFlow();
        this.flowStep = context.getFlowStep();
    }

    protected JdbcTemplate getJdbcTemplate() {
        if (resource == null) {
            throw new IllegalStateException("In order to create a jdbc template, a datasource resource must be defined");
        }
        DataSource ds = resource.reference();
        return new JdbcTemplate(ds);
    }

    protected BasicDataSource getBasicDataSource() {
        return (BasicDataSource) resource.reference();
    }

    protected Row nextRowFromInputMessage() {
        if (flowStep.getComponent().getInputModel() != null) {
            if (entityDataIterator == null) {
                List<EntityData> list = inputMessage.getPayload();
                entityDataIterator = list.iterator();
            }

            if (entityDataIterator.hasNext()) {
                EntityData data = entityDataIterator.next();
                return flowStep.getComponent().toRow(data, false);
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException(
                    "The input model needs to be set if you are going to use the entity data");
        }
    }

    protected void info(String message, Object... args) {
        context.getExecutionTracker().log(LogLevel.INFO, context, message, args);
    }

    protected void setInputMessage(Message inputMessage) {
        this.inputMessage = inputMessage;
    }

    protected Object getAttributeValue(String entityName, String attributeName) {
        Model model = flowStep.getComponent().getInputModel();
        ArrayList<EntityData> rows = inputMessage.getPayload();
        return ComponentUtil.getAttributeValue(model, rows, entityName, attributeName);
    }

    protected List<Object> getAttributeValues(String entityName, String attributeName) {
        Model model = flowStep.getComponent().getInputModel();
        ArrayList<EntityData> rows = inputMessage.getPayload();
        return ComponentUtil.getAttributeValues(model, rows, entityName, attributeName);
    }
    
    protected void setMessageTarget(IMessageTarget messageTarget) {
        this.messageTarget = messageTarget;
    }

    protected void onInit() {
    }

    protected void onHandle() {        
    }

    protected void onError(Throwable myError) {
    }
    
    protected void onSuccess() {
    }

}