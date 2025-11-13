-- LeetMate Platform Database Initialization Script
-- This script clears existing data and initializes the database with test data

-- Clear existing data (in reverse order of dependencies)
TRUNCATE TABLE submission_review_suggestions CASCADE;
TRUNCATE TABLE submission_reviews CASCADE;
TRUNCATE TABLE submissions CASCADE;
TRUNCATE TABLE challenges CASCADE;
TRUNCATE TABLE group_members CASCADE;
TRUNCATE TABLE study_group_tags CASCADE;
TRUNCATE TABLE study_groups CASCADE;
TRUNCATE TABLE users CASCADE;

-- Reset sequences if any (PostgreSQL doesn't use sequences for UUID, but included for completeness)

-- Insert test users
-- Password for all test users: "password"
-- Bcrypt hash generated using BCryptPasswordEncoder (strength 10)
INSERT INTO users (id, name, email, password_hash, role, created_at) VALUES
('11111111-1111-1111-1111-111111111111', 'Mentor One', 'mentor1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MENTOR', NOW()),
('22222222-2222-2222-2222-222222222222', 'Mentor Two', 'mentor2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MENTOR', NOW()),
('33333333-3333-3333-3333-333333333333', 'Mentee One', 'mentee1@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MENTEE', NOW()),
('44444444-4444-4444-4444-444444444444', 'Mentee Two', 'mentee2@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MENTEE', NOW()),
('55555555-5555-5555-5555-555555555555', 'Mentee Three', 'mentee3@test.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MENTEE', NOW());

-- Insert test study groups
INSERT INTO study_groups (id, name, description, mentor_id, member_count, created_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Graph Algorithms', 'Learn graph algorithms and data structures', '11111111-1111-1111-1111-111111111111', 2, NOW()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Dynamic Programming', 'Master DP techniques and problem-solving', '11111111-1111-1111-1111-111111111111', 1, NOW()),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'System Design', 'Learn system design principles', '22222222-2222-2222-2222-222222222222', 1, NOW());

-- Insert study group tags
INSERT INTO study_group_tags (group_id, tag) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'graph'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'algorithms'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'advanced'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'dp'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'algorithms'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'system-design'),
('cccccccc-cccc-cccc-cccc-cccccccccccc', 'backend');

-- Insert group members (mentees joining groups)
INSERT INTO group_members (id, group_id, member_id, joined_at) VALUES
('10000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '33333333-3333-3333-3333-333333333333', NOW() - INTERVAL '2 days'),
('10000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '44444444-4444-4444-4444-444444444444', NOW() - INTERVAL '1 day'),
('10000000-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', NOW() - INTERVAL '3 days'),
('10000000-0000-0000-0000-000000000004', 'cccccccc-cccc-cccc-cccc-cccccccccccc', '55555555-5555-5555-5555-555555555555', NOW() - INTERVAL '1 day');

-- Insert challenges
INSERT INTO challenges (id, group_id, title, description, language, difficulty, starter_code, created_at) VALUES
('20000000-0000-0000-0000-000000000001', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Two Sum', 'Find two numbers that add up to target', 'java', 'EASY', 'class Solution { public int[] twoSum(int[] nums, int target) { } }', NOW() - INTERVAL '5 days'),
('20000000-0000-0000-0000-000000000002', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Course Schedule', 'Detect cycle in directed graph', 'java', 'MEDIUM', 'class Solution { public boolean canFinish(int numCourses, int[][] prerequisites) { } }', NOW() - INTERVAL '3 days'),
('20000000-0000-0000-0000-000000000003', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'Climbing Stairs', 'Classic DP problem', 'java', 'EASY', 'class Solution { public int climbStairs(int n) { } }', NOW() - INTERVAL '4 days');

-- Update member counts to match actual members
UPDATE study_groups SET member_count = (SELECT COUNT(*) FROM group_members WHERE group_id = study_groups.id);

