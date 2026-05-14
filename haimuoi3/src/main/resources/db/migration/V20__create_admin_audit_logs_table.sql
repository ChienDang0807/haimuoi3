-- Create admin_audit_logs table for tracking all sysadmin actions
CREATE TABLE admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_admin_audit_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX idx_admin_audit_logs_user_id ON admin_audit_logs(user_id);
CREATE INDEX idx_admin_audit_logs_action ON admin_audit_logs(action);
CREATE INDEX idx_admin_audit_logs_entity_type ON admin_audit_logs(entity_type);
CREATE INDEX idx_admin_audit_logs_created_at ON admin_audit_logs(created_at);
CREATE INDEX idx_admin_audit_logs_entity_id ON admin_audit_logs(entity_id);

-- Add comment to table
COMMENT ON TABLE admin_audit_logs IS 'Audit log for tracking all sysadmin actions';
COMMENT ON COLUMN admin_audit_logs.user_id IS 'ID of the sysadmin who performed the action';
COMMENT ON COLUMN admin_audit_logs.action IS 'Action performed (CREATE, UPDATE, DELETE, SUSPEND, ACTIVATE, etc.)';
COMMENT ON COLUMN admin_audit_logs.entity_type IS 'Type of entity affected (USER, SHOP, CATEGORY, etc.)';
COMMENT ON COLUMN admin_audit_logs.entity_id IS 'ID of the affected entity';
COMMENT ON COLUMN admin_audit_logs.old_value IS 'JSON representation of old value (for UPDATE actions)';
COMMENT ON COLUMN admin_audit_logs.new_value IS 'JSON representation of new value (for CREATE/UPDATE actions)';
COMMENT ON COLUMN admin_audit_logs.ip_address IS 'IP address of the sysadmin';
COMMENT ON COLUMN admin_audit_logs.created_at IS 'Timestamp when the action was performed';
