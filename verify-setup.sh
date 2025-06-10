#!/bin/bash

echo "🔍 Verificando configuración completa del laboratorio..."
echo "======================================================="

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 1. Verificar contenedores
print_status "Verificando estado de contenedores..."
if docker-compose ps | grep -q "Up"; then
    print_success "Contenedores están ejecutándose"
    docker-compose ps
else
    print_error "Algunos contenedores no están ejecutándose"
    docker-compose ps
fi

echo ""

# 2. Verificar PostgreSQL
print_status "Verificando conexión a PostgreSQL..."
if docker-compose exec -T postgres pg_isready -U postgres -d postgres; then
    print_success "PostgreSQL está listo"
else
    print_error "PostgreSQL no está disponible"
    exit 1
fi

# 3. Verificar base de datos y usuario
print_status "Verificando base de datos jesus_maria_workshops..."
DB_EXISTS=$(docker-compose exec -T postgres psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='jesus_maria_workshops'")
if [ "$DB_EXISTS" = "1" ]; then
    print_success "Base de datos jesus_maria_workshops existe"
else
    print_error "Base de datos jesus_maria_workshops NO existe"
    exit 1
fi

# 4. Verificar usuario workshop_user
print_status "Verificando usuario workshop_user..."
USER_EXISTS=$(docker-compose exec -T postgres psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_user WHERE usename='workshop_user'")
if [ "$USER_EXISTS" = "1" ]; then
    print_success "Usuario workshop_user existe"
else
    print_error "Usuario workshop_user NO existe"
    exit 1
fi

# 5. Verificar tablas
print_status "Verificando tablas en la base de datos..."
TABLES_COUNT=$(docker-compose exec -T postgres psql -U workshop_user -d jesus_maria_workshops -tAc "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE'")
if [ "$TABLES_COUNT" -ge "2" ]; then
    print_success "Tablas creadas correctamente ($TABLES_COUNT tablas encontradas)"
    docker-compose exec -T postgres psql -U workshop_user -d jesus_maria_workshops -c "\dt"
else
    print_error "No se encontraron suficientes tablas ($TABLES_COUNT)"
fi

# 6. Verificar Spark Master
print_status "Verificando Spark Master..."
if curl -s http://localhost:8080/json/ | grep -q '"status"'; then
    print_success "Spark Master está respondiendo"
else
    print_error "Spark Master no está disponible"
fi

echo ""
print_status "=== RESUMEN DE VERIFICACIÓN ==="
echo "🔗 URLs disponibles:"
echo "   • Spark Master UI: http://localhost:8080"
echo "   • Jupyter Lab: http://localhost:8888 (token: workshop2025)"
echo "   • PgAdmin: http://localhost:5050"
echo ""
echo "🔑 Credenciales PostgreSQL:"
echo "   • Host: localhost:5432"
echo "   • Database: jesus_maria_workshops"
echo "   • User: workshop_user"
echo "   • Password: workshop_pass"
echo ""
print_success "✅ Verificación completa terminada!"