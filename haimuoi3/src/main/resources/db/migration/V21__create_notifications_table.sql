CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    notification_type VARCHAR(64) NOT NULL,
    recipient_kind VARCHAR(16) NOT NULL,
    recipient_id BIGINT NOT NULL,
    recipient_role VARCHAR(32) NOT NULL,
    payload JSONB NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications (recipient_kind, recipient_id, created_at DESC);

CREATE INDEX idx_notifications_recipient_unread
    ON notifications (recipient_kind, recipient_id, is_read);
