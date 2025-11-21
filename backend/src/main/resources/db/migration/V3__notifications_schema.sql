-- Create notifications table for group thread and reply notifications
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    type VARCHAR(20) NOT NULL CHECK (type IN ('NEW_THREAD', 'THREAD_REPLY')),
    group_id UUID REFERENCES study_groups(id),
    thread_id UUID NOT NULL REFERENCES chat_threads(id),
    message_id UUID REFERENCES chat_messages(id),
    actor_id UUID NOT NULL REFERENCES users(id),
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_notifications_user_id_read ON notifications(user_id, read);
CREATE INDEX idx_notifications_user_id_created_at ON notifications(user_id, created_at DESC);
CREATE INDEX idx_notifications_user_id_type ON notifications(user_id, type);


