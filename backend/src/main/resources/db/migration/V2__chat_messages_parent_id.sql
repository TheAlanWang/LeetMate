-- Adds reply relationship for chat messages
alter table chat_messages
    add column if not exists parent_id uuid;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_chat_messages_parent'
          AND table_name = 'chat_messages'
    ) THEN
        ALTER TABLE chat_messages
            ADD CONSTRAINT fk_chat_messages_parent
            FOREIGN KEY (parent_id) REFERENCES chat_messages(id);
    END IF;
END $$;
