#!/bin/bash

# LeetMate Platform Database Initialization Script
# This script initializes the database with test data

set -e

echo "🚀 Initializing LeetMate database..."

# Check if docker is running
if ! docker ps > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if database container exists
if ! docker ps | grep -q leetmate-db; then
    echo "📦 Starting database container..."
    cd "$(dirname "$0")/.."
    docker compose up -d db
    echo "⏳ Waiting for database to be ready..."
    sleep 5
fi

# Get the database container name
DB_CONTAINER=$(docker ps --format "{{.Names}}" | grep -E "leetmate-db|postgres" | head -1)

if [ -z "$DB_CONTAINER" ]; then
    echo "❌ Database container not found. Please start it first with: docker compose up -d db"
    exit 1
fi

echo "📊 Database container: $DB_CONTAINER"

# Check if we should clear existing data
if [ "$1" = "--reset" ] || [ "$1" = "-r" ]; then
    echo "🗑️  Clearing existing data..."
    docker exec -i "$DB_CONTAINER" psql -U postgres -d leetmate <<EOF
TRUNCATE TABLE submission_review_suggestions CASCADE;
TRUNCATE TABLE submission_reviews CASCADE;
TRUNCATE TABLE submissions CASCADE;
TRUNCATE TABLE challenges CASCADE;
TRUNCATE TABLE group_members CASCADE;
TRUNCATE TABLE study_group_tags CASCADE;
TRUNCATE TABLE study_groups CASCADE;
TRUNCATE TABLE users CASCADE;
EOF
    echo "✅ Existing data cleared"
fi

# Run initialization script
INIT_SCRIPT="src/main/resources/db-init.sql"

if [ -f "$INIT_SCRIPT" ]; then
    echo "📝 Running initialization script..."
    docker exec -i "$DB_CONTAINER" psql -U postgres -d leetmate < "$INIT_SCRIPT"
    echo "✅ Database initialized successfully!"
else
    echo "⚠️  Initialization script not found at $INIT_SCRIPT"
    echo "💡 Creating tables using Hibernate (start the Spring Boot app)..."
fi

# Show current data counts
echo ""
echo "📈 Current database statistics:"
docker exec "$DB_CONTAINER" psql -U postgres -d leetmate -c "
SELECT 
    (SELECT COUNT(*) FROM users) as users,
    (SELECT COUNT(*) FROM study_groups) as groups,
    (SELECT COUNT(*) FROM group_members) as memberships,
    (SELECT COUNT(*) FROM challenges) as challenges;
"

echo ""
echo "✨ Done! You can now start the Spring Boot application."
echo "💡 Test users:"
echo "   - mentor1@test.com / password (MENTOR)"
echo "   - mentee1@test.com / password (MENTEE)"

