-- Baseline schema for LeetMate (matches current JPA entities)

create table users (
    id uuid primary key,
    name varchar(80) not null,
    email varchar(160) not null unique,
    password_hash varchar(200) not null,
    role varchar(20) not null,
    created_at timestamptz not null
);

create table study_groups (
    id uuid primary key,
    name varchar(80) not null,
    description varchar(400) not null,
    mentor_id uuid not null references users(id),
    member_count int not null,
    created_at timestamptz not null
);

create table study_group_tags (
    group_id uuid not null references study_groups(id),
    tag varchar(30) not null,
    primary key (group_id, tag)
);

create table group_members (
    id uuid primary key,
    group_id uuid not null references study_groups(id),
    member_id uuid not null references users(id),
    joined_at timestamptz not null,
    unique (group_id, member_id)
);

create table challenges (
    id uuid primary key,
    group_id uuid not null references study_groups(id),
    title varchar(120) not null,
    description text not null,
    language varchar(20) not null,
    difficulty varchar(10) not null check (difficulty in ('EASY','MEDIUM','HARD')),
    starter_code text not null,
    created_at timestamptz not null
);

create table submission_reviews (
    id uuid primary key,
    created_at timestamptz not null,
    summary text not null,
    complexity int not null
);

create table submission_review_suggestions (
    review_id uuid not null references submission_reviews(id),
    suggestion text not null
);

create table submissions (
    id uuid primary key,
    challenge_id uuid not null references challenges(id),
    mentee_id uuid not null references users(id),
    language varchar(20) not null,
    code text not null,
    credits_awarded int not null,
    created_at timestamptz not null,
    review_id uuid references submission_reviews(id)
);

create table chat_threads (
    id uuid primary key,
    group_id uuid not null references study_groups(id),
    created_by uuid not null references users(id),
    title varchar(160) not null,
    description text,
    created_at timestamptz not null
);

create table chat_messages (
    id uuid primary key,
    thread_id uuid not null references chat_threads(id),
    author_id uuid not null references users(id),
    parent_id uuid references chat_messages(id),
    content text not null,
    code_language varchar(30),
    created_at timestamptz not null
);

create table password_reset_tokens (
    token uuid primary key,
    user_id uuid not null references users(id),
    created_at timestamptz not null,
    expires_at timestamptz not null,
    used_at timestamptz
);
