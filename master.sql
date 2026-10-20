--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adicionales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.adicionales (
    id bigint NOT NULL,
    grupo_id bigint NOT NULL,
    nombre character varying(80) NOT NULL,
    precio_adicional numeric(12,2) DEFAULT 0 NOT NULL,
    costo_adicional numeric(12,2) DEFAULT 0 NOT NULL,
    controla_stock boolean DEFAULT false NOT NULL,
    stock_actual numeric(12,2) DEFAULT 0 NOT NULL,
    stock_minimo numeric(12,2) DEFAULT 0 NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT adicionales_costo_adicional_check CHECK ((costo_adicional >= (0)::numeric)),
    CONSTRAINT adicionales_precio_adicional_check CHECK ((precio_adicional >= (0)::numeric))
);


--
-- Name: adicionales_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.adicionales_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: adicionales_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.adicionales_id_seq OWNED BY public.adicionales.id;


--
-- Name: cajas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.cajas (
    id bigint NOT NULL,
    estado character varying(20) DEFAULT 'ABIERTA'::character varying NOT NULL,
    monto_apertura numeric(12,2) NOT NULL,
    monto_cierre_esperado numeric(12,2),
    monto_cierre_contado numeric(12,2),
    diferencia numeric(12,2),
    total_ventas numeric(12,2),
    observacion character varying(300),
    usuario_apertura_id bigint,
    usuario_cierre_id bigint,
    abierta_en timestamp with time zone DEFAULT now() NOT NULL,
    cerrada_en timestamp with time zone,
    CONSTRAINT cajas_estado_check CHECK (((estado)::text = ANY ((ARRAY['ABIERTA'::character varying, 'CERRADA'::character varying])::text[]))),
    CONSTRAINT cajas_monto_apertura_check CHECK ((monto_apertura >= (0)::numeric))
);


--
-- Name: cajas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.cajas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: cajas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.cajas_id_seq OWNED BY public.cajas.id;


--
-- Name: compra_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.compra_items (
    id bigint NOT NULL,
    compra_id bigint NOT NULL,
    insumo_id bigint,
    producto_id bigint,
    nombre_momento character varying(120) NOT NULL,
    cantidad numeric(12,4) NOT NULL,
    costo_unitario numeric(12,4) NOT NULL,
    subtotal numeric(12,2) NOT NULL,
    CONSTRAINT compra_items_cantidad_check CHECK ((cantidad > (0)::numeric)),
    CONSTRAINT compra_items_check CHECK ((((insumo_id IS NOT NULL) AND (producto_id IS NULL)) OR ((insumo_id IS NULL) AND (producto_id IS NOT NULL)))),
    CONSTRAINT compra_items_costo_unitario_check CHECK ((costo_unitario >= (0)::numeric)),
    CONSTRAINT compra_items_subtotal_check CHECK ((subtotal >= (0)::numeric))
);


--
-- Name: compra_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.compra_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: compra_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.compra_items_id_seq OWNED BY public.compra_items.id;


--
-- Name: compras; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.compras (
    id bigint NOT NULL,
    proveedor character varying(120),
    numero_factura character varying(50),
    metodo_pago character varying(30),
    observaciones character varying(300),
    total numeric(12,2) NOT NULL,
    usuario_id bigint,
    usuario_nombre_momento character varying(80),
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT compras_total_check CHECK ((total >= (0)::numeric))
);


--
-- Name: compras_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.compras_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: compras_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.compras_id_seq OWNED BY public.compras.id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


--
-- Name: gastos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.gastos (
    id bigint NOT NULL,
    categoria character varying(80),
    descripcion character varying(300) NOT NULL,
    monto numeric(12,2) NOT NULL,
    metodo_pago character varying(30),
    observacion character varying(300),
    usuario_id bigint,
    usuario_nombre_momento character varying(80),
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT gastos_monto_check CHECK ((monto > (0)::numeric))
);


--
-- Name: gastos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.gastos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: gastos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.gastos_id_seq OWNED BY public.gastos.id;


--
-- Name: grupos_adicionales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.grupos_adicionales (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    nombre character varying(80) NOT NULL,
    min_seleccion integer DEFAULT 0 NOT NULL,
    max_seleccion integer DEFAULT 1 NOT NULL,
    CONSTRAINT grupos_adicionales_max_seleccion_check CHECK ((max_seleccion >= 1)),
    CONSTRAINT grupos_adicionales_min_seleccion_check CHECK ((min_seleccion >= 0))
);


--
-- Name: grupos_adicionales_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.grupos_adicionales_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: grupos_adicionales_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.grupos_adicionales_id_seq OWNED BY public.grupos_adicionales.id;


--
-- Name: insumos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.insumos (
    id bigint NOT NULL,
    nombre character varying(120) NOT NULL,
    unidad_medida character varying(20) DEFAULT 'UNIDAD'::character varying NOT NULL,
    costo_unitario numeric(12,4) NOT NULL,
    stock_actual numeric(12,4) DEFAULT 0 NOT NULL,
    stock_minimo numeric(12,4) DEFAULT 0 NOT NULL,
    proveedor character varying(120),
    activo boolean DEFAULT true NOT NULL,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT insumos_costo_unitario_check CHECK ((costo_unitario >= (0)::numeric)),
    CONSTRAINT insumos_stock_actual_check CHECK ((stock_actual >= (0)::numeric)),
    CONSTRAINT insumos_unidad_medida_check CHECK (((unidad_medida)::text = ANY ((ARRAY['UNIDAD'::character varying, 'GRAMO'::character varying, 'KILOGRAMO'::character varying, 'MILILITRO'::character varying, 'LITRO'::character varying])::text[])))
);


--
-- Name: insumos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.insumos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: insumos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.insumos_id_seq OWNED BY public.insumos.id;


--
-- Name: mermas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.mermas (
    id bigint NOT NULL,
    insumo_id bigint,
    producto_id bigint,
    nombre_momento character varying(120) NOT NULL,
    cantidad numeric(12,4) NOT NULL,
    costo_unitario numeric(12,4) NOT NULL,
    costo_total numeric(12,2) NOT NULL,
    motivo character varying(30) NOT NULL,
    observacion character varying(300),
    usuario_id bigint,
    usuario_nombre_momento character varying(80),
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT mermas_cantidad_check CHECK ((cantidad > (0)::numeric)),
    CONSTRAINT mermas_check CHECK ((((insumo_id IS NOT NULL) AND (producto_id IS NULL)) OR ((insumo_id IS NULL) AND (producto_id IS NOT NULL)))),
    CONSTRAINT mermas_costo_total_check CHECK ((costo_total >= (0)::numeric)),
    CONSTRAINT mermas_costo_unitario_check CHECK ((costo_unitario >= (0)::numeric))
);


--
-- Name: mermas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.mermas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: mermas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.mermas_id_seq OWNED BY public.mermas.id;


--
-- Name: movimientos_caja; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.movimientos_caja (
    id bigint NOT NULL,
    caja_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    monto numeric(12,2) NOT NULL,
    motivo character varying(300) NOT NULL,
    usuario_id bigint,
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    usuario_nombre_momento character varying(80),
    CONSTRAINT movimientos_caja_monto_check CHECK ((monto > (0)::numeric)),
    CONSTRAINT movimientos_caja_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['RETIRO'::character varying, 'INGRESO'::character varying, 'GASTO'::character varying])::text[])))
);


--
-- Name: movimientos_caja_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.movimientos_caja_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: movimientos_caja_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.movimientos_caja_id_seq OWNED BY public.movimientos_caja.id;


--
-- Name: movimientos_inventario; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.movimientos_inventario (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    tipo character varying(20) NOT NULL,
    cantidad numeric(12,2) NOT NULL,
    motivo character varying(200),
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT movimientos_inventario_tipo_check CHECK (((tipo)::text = ANY ((ARRAY['ENTRADA'::character varying, 'SALIDA'::character varying, 'AJUSTE'::character varying])::text[])))
);


--
-- Name: movimientos_inventario_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.movimientos_inventario_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: movimientos_inventario_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.movimientos_inventario_id_seq OWNED BY public.movimientos_inventario.id;


--
-- Name: producto_variantes; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.producto_variantes (
    id bigint NOT NULL,
    producto_id bigint NOT NULL,
    nombre character varying(80) NOT NULL,
    precio numeric(12,2) NOT NULL,
    costo numeric(12,2) NOT NULL,
    sku character varying(50),
    stock_actual numeric(12,2) DEFAULT 0 NOT NULL,
    stock_minimo numeric(12,2) DEFAULT 0 NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    CONSTRAINT producto_variantes_costo_check CHECK ((costo >= (0)::numeric)),
    CONSTRAINT producto_variantes_precio_check CHECK ((precio >= (0)::numeric)),
    CONSTRAINT producto_variantes_stock_actual_check CHECK ((stock_actual >= (0)::numeric))
);


--
-- Name: producto_variantes_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.producto_variantes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: producto_variantes_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.producto_variantes_id_seq OWNED BY public.producto_variantes.id;


--
-- Name: productos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.productos (
    id bigint NOT NULL,
    nombre character varying(120) NOT NULL,
    categoria character varying(60),
    costo_unitario numeric(12,2) NOT NULL,
    precio_venta numeric(12,2) NOT NULL,
    unidad_medida character varying(20) DEFAULT 'unidad'::character varying NOT NULL,
    stock_actual numeric(12,2) DEFAULT 0 NOT NULL,
    stock_minimo numeric(12,2) DEFAULT 0 NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    actualizado_en timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT productos_costo_unitario_check CHECK ((costo_unitario >= (0)::numeric)),
    CONSTRAINT productos_precio_venta_check CHECK ((precio_venta >= (0)::numeric)),
    CONSTRAINT productos_stock_actual_check CHECK ((stock_actual >= (0)::numeric))
);


--
-- Name: productos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.productos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: productos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.productos_id_seq OWNED BY public.productos.id;


--
-- Name: promociones; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.promociones (
    id bigint NOT NULL,
    nombre character varying(120) NOT NULL,
    tipo character varying(30) NOT NULL,
    valor numeric(12,2) NOT NULL,
    producto_id bigint,
    categoria character varying(80),
    fecha_inicio date,
    fecha_fin date,
    cantidad_minima numeric(12,2) DEFAULT 1 NOT NULL,
    activo boolean DEFAULT true NOT NULL,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    CONSTRAINT promociones_cantidad_minima_check CHECK ((cantidad_minima > (0)::numeric)),
    CONSTRAINT promociones_valor_check CHECK ((valor >= (0)::numeric))
);


--
-- Name: promociones_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.promociones_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: promociones_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.promociones_id_seq OWNED BY public.promociones.id;


--
-- Name: receta_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.receta_items (
    id bigint NOT NULL,
    producto_id bigint,
    variante_id bigint,
    insumo_id bigint NOT NULL,
    cantidad numeric(12,4) NOT NULL,
    CONSTRAINT receta_items_cantidad_check CHECK ((cantidad > (0)::numeric)),
    CONSTRAINT receta_items_check CHECK ((((producto_id IS NOT NULL) AND (variante_id IS NULL)) OR ((producto_id IS NULL) AND (variante_id IS NOT NULL))))
);


--
-- Name: receta_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.receta_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: receta_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.receta_items_id_seq OWNED BY public.receta_items.id;


--
-- Name: registros_auditoria; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.registros_auditoria (
    id bigint NOT NULL,
    usuario_id bigint,
    usuario_nombre_momento character varying(80),
    accion character varying(30) NOT NULL,
    entidad character varying(40),
    entidad_id bigint,
    detalle character varying(500),
    ip character varying(45),
    fecha timestamp with time zone DEFAULT now() NOT NULL
);


--
-- Name: registros_auditoria_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.registros_auditoria_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: registros_auditoria_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.registros_auditoria_id_seq OWNED BY public.registros_auditoria.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    username character varying(60) NOT NULL,
    password_hash character varying(100) NOT NULL,
    creado_en timestamp with time zone DEFAULT now() NOT NULL,
    rol character varying(20) DEFAULT 'ADMIN'::character varying NOT NULL
);


--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: venta_item_adicionales; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.venta_item_adicionales (
    id bigint NOT NULL,
    venta_item_id bigint NOT NULL,
    adicional_id bigint,
    nombre_momento character varying(80) NOT NULL,
    precio_adicional_momento numeric(12,2) NOT NULL,
    cantidad_stock_descontada numeric(12,4),
    costo_adicional_momento numeric(12,4) DEFAULT 0 NOT NULL
);


--
-- Name: venta_item_adicionales_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.venta_item_adicionales_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: venta_item_adicionales_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.venta_item_adicionales_id_seq OWNED BY public.venta_item_adicionales.id;


--
-- Name: venta_item_insumos; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.venta_item_insumos (
    id bigint NOT NULL,
    venta_item_id bigint NOT NULL,
    insumo_id bigint NOT NULL,
    nombre_momento character varying(120) NOT NULL,
    cantidad_consumida numeric(12,4) NOT NULL
);


--
-- Name: venta_item_insumos_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.venta_item_insumos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: venta_item_insumos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.venta_item_insumos_id_seq OWNED BY public.venta_item_insumos.id;


--
-- Name: venta_items; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.venta_items (
    id bigint NOT NULL,
    venta_id bigint NOT NULL,
    producto_id bigint NOT NULL,
    cantidad numeric(12,2) NOT NULL,
    precio_unitario_momento numeric(12,2) NOT NULL,
    costo_unitario_momento numeric(12,2) NOT NULL,
    variante_id bigint,
    variante_nombre_momento character varying(80),
    promocion_id bigint,
    promocion_nombre_momento character varying(120),
    CONSTRAINT venta_items_cantidad_check CHECK ((cantidad > (0)::numeric)),
    CONSTRAINT venta_items_costo_unitario_momento_check CHECK ((costo_unitario_momento >= (0)::numeric)),
    CONSTRAINT venta_items_precio_unitario_momento_check CHECK ((precio_unitario_momento >= (0)::numeric))
);


--
-- Name: venta_items_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.venta_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: venta_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.venta_items_id_seq OWNED BY public.venta_items.id;


--
-- Name: ventas; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.ventas (
    id bigint NOT NULL,
    fecha timestamp with time zone DEFAULT now() NOT NULL,
    total numeric(12,2) NOT NULL,
    metodo_pago character varying(30) DEFAULT 'EFECTIVO'::character varying NOT NULL,
    usuario_id bigint,
    caja_id bigint,
    impreso_en timestamp with time zone,
    estado character varying(20) DEFAULT 'CONFIRMADA'::character varying NOT NULL,
    anulada_en timestamp with time zone,
    anulada_por_id bigint,
    motivo_anulacion character varying(500),
    CONSTRAINT ventas_estado_check CHECK (((estado)::text = ANY ((ARRAY['CONFIRMADA'::character varying, 'ANULADA'::character varying])::text[]))),
    CONSTRAINT ventas_total_check CHECK ((total >= (0)::numeric))
);


--
-- Name: ventas_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.ventas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--

ALTER SEQUENCE public.ventas_id_seq OWNED BY public.ventas.id;


--
-- Name: adicionales id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adicionales ALTER COLUMN id SET DEFAULT nextval('public.adicionales_id_seq'::regclass);


--
-- Name: cajas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cajas ALTER COLUMN id SET DEFAULT nextval('public.cajas_id_seq'::regclass);


--
-- Name: compra_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compra_items ALTER COLUMN id SET DEFAULT nextval('public.compra_items_id_seq'::regclass);


--
-- Name: compras id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compras ALTER COLUMN id SET DEFAULT nextval('public.compras_id_seq'::regclass);


--
-- Name: gastos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gastos ALTER COLUMN id SET DEFAULT nextval('public.gastos_id_seq'::regclass);


--
-- Name: grupos_adicionales id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grupos_adicionales ALTER COLUMN id SET DEFAULT nextval('public.grupos_adicionales_id_seq'::regclass);


--
-- Name: insumos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.insumos ALTER COLUMN id SET DEFAULT nextval('public.insumos_id_seq'::regclass);


--
-- Name: mermas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mermas ALTER COLUMN id SET DEFAULT nextval('public.mermas_id_seq'::regclass);


--
-- Name: movimientos_caja id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_caja ALTER COLUMN id SET DEFAULT nextval('public.movimientos_caja_id_seq'::regclass);


--
-- Name: movimientos_inventario id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario ALTER COLUMN id SET DEFAULT nextval('public.movimientos_inventario_id_seq'::regclass);


--
-- Name: producto_variantes id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto_variantes ALTER COLUMN id SET DEFAULT nextval('public.producto_variantes_id_seq'::regclass);


--
-- Name: productos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productos ALTER COLUMN id SET DEFAULT nextval('public.productos_id_seq'::regclass);


--
-- Name: promociones id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promociones ALTER COLUMN id SET DEFAULT nextval('public.promociones_id_seq'::regclass);


--
-- Name: receta_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receta_items ALTER COLUMN id SET DEFAULT nextval('public.receta_items_id_seq'::regclass);


--
-- Name: registros_auditoria id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registros_auditoria ALTER COLUMN id SET DEFAULT nextval('public.registros_auditoria_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Name: venta_item_adicionales id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_adicionales ALTER COLUMN id SET DEFAULT nextval('public.venta_item_adicionales_id_seq'::regclass);


--
-- Name: venta_item_insumos id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_insumos ALTER COLUMN id SET DEFAULT nextval('public.venta_item_insumos_id_seq'::regclass);


--
-- Name: venta_items id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items ALTER COLUMN id SET DEFAULT nextval('public.venta_items_id_seq'::regclass);


--
-- Name: ventas id; Type: DEFAULT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas ALTER COLUMN id SET DEFAULT nextval('public.ventas_id_seq'::regclass);


--
-- Name: adicionales adicionales_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adicionales
    ADD CONSTRAINT adicionales_pkey PRIMARY KEY (id);


--
-- Name: cajas cajas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cajas
    ADD CONSTRAINT cajas_pkey PRIMARY KEY (id);


--
-- Name: compra_items compra_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compra_items
    ADD CONSTRAINT compra_items_pkey PRIMARY KEY (id);


--
-- Name: compras compras_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: gastos gastos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gastos
    ADD CONSTRAINT gastos_pkey PRIMARY KEY (id);


--
-- Name: grupos_adicionales grupos_adicionales_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grupos_adicionales
    ADD CONSTRAINT grupos_adicionales_pkey PRIMARY KEY (id);


--
-- Name: insumos insumos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.insumos
    ADD CONSTRAINT insumos_pkey PRIMARY KEY (id);


--
-- Name: mermas mermas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mermas
    ADD CONSTRAINT mermas_pkey PRIMARY KEY (id);


--
-- Name: movimientos_caja movimientos_caja_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_caja
    ADD CONSTRAINT movimientos_caja_pkey PRIMARY KEY (id);


--
-- Name: movimientos_inventario movimientos_inventario_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT movimientos_inventario_pkey PRIMARY KEY (id);


--
-- Name: producto_variantes producto_variantes_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto_variantes
    ADD CONSTRAINT producto_variantes_pkey PRIMARY KEY (id);


--
-- Name: productos productos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.productos
    ADD CONSTRAINT productos_pkey PRIMARY KEY (id);


--
-- Name: promociones promociones_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promociones
    ADD CONSTRAINT promociones_pkey PRIMARY KEY (id);


--
-- Name: receta_items receta_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receta_items
    ADD CONSTRAINT receta_items_pkey PRIMARY KEY (id);


--
-- Name: registros_auditoria registros_auditoria_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registros_auditoria
    ADD CONSTRAINT registros_auditoria_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: usuarios usuarios_username_key; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_username_key UNIQUE (username);


--
-- Name: venta_item_adicionales venta_item_adicionales_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_adicionales
    ADD CONSTRAINT venta_item_adicionales_pkey PRIMARY KEY (id);


--
-- Name: venta_item_insumos venta_item_insumos_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_insumos
    ADD CONSTRAINT venta_item_insumos_pkey PRIMARY KEY (id);


--
-- Name: venta_items venta_items_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items
    ADD CONSTRAINT venta_items_pkey PRIMARY KEY (id);


--
-- Name: ventas ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: idx_adicionales_grupo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_adicionales_grupo ON public.adicionales USING btree (grupo_id);


--
-- Name: idx_auditoria_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auditoria_fecha ON public.registros_auditoria USING btree (fecha DESC);


--
-- Name: idx_auditoria_usuario; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_auditoria_usuario ON public.registros_auditoria USING btree (usuario_id);


--
-- Name: idx_compra_items_compra; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_compra_items_compra ON public.compra_items USING btree (compra_id);


--
-- Name: idx_compras_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_compras_fecha ON public.compras USING btree (fecha DESC);


--
-- Name: idx_gastos_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_gastos_fecha ON public.gastos USING btree (fecha DESC);


--
-- Name: idx_grupos_producto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_grupos_producto ON public.grupos_adicionales USING btree (producto_id);


--
-- Name: idx_mermas_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_mermas_fecha ON public.mermas USING btree (fecha DESC);


--
-- Name: idx_movimientos_caja_caja; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movimientos_caja_caja ON public.movimientos_caja USING btree (caja_id);


--
-- Name: idx_movimientos_producto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_movimientos_producto ON public.movimientos_inventario USING btree (producto_id);


--
-- Name: idx_promociones_activo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_promociones_activo ON public.promociones USING btree (activo);


--
-- Name: idx_receta_insumo; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_receta_insumo ON public.receta_items USING btree (insumo_id);


--
-- Name: idx_receta_producto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_receta_producto ON public.receta_items USING btree (producto_id);


--
-- Name: idx_receta_variante; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_receta_variante ON public.receta_items USING btree (variante_id);


--
-- Name: idx_variantes_producto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_variantes_producto ON public.producto_variantes USING btree (producto_id);


--
-- Name: idx_venta_item_adicionales_item; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_venta_item_adicionales_item ON public.venta_item_adicionales USING btree (venta_item_id);


--
-- Name: idx_venta_item_insumos_item; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_venta_item_insumos_item ON public.venta_item_insumos USING btree (venta_item_id);


--
-- Name: idx_venta_items_producto; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_venta_items_producto ON public.venta_items USING btree (producto_id);


--
-- Name: idx_venta_items_venta; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_venta_items_venta ON public.venta_items USING btree (venta_id);


--
-- Name: idx_ventas_caja; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ventas_caja ON public.ventas USING btree (caja_id);


--
-- Name: idx_ventas_fecha; Type: INDEX; Schema: public; Owner: -
--

CREATE INDEX idx_ventas_fecha ON public.ventas USING btree (fecha);


--
-- Name: uq_una_caja_abierta; Type: INDEX; Schema: public; Owner: -
--

CREATE UNIQUE INDEX uq_una_caja_abierta ON public.cajas USING btree (estado) WHERE ((estado)::text = 'ABIERTA'::text);


--
-- Name: adicionales adicionales_grupo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.adicionales
    ADD CONSTRAINT adicionales_grupo_id_fkey FOREIGN KEY (grupo_id) REFERENCES public.grupos_adicionales(id) ON DELETE CASCADE;


--
-- Name: cajas cajas_usuario_apertura_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cajas
    ADD CONSTRAINT cajas_usuario_apertura_id_fkey FOREIGN KEY (usuario_apertura_id) REFERENCES public.usuarios(id);


--
-- Name: cajas cajas_usuario_cierre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.cajas
    ADD CONSTRAINT cajas_usuario_cierre_id_fkey FOREIGN KEY (usuario_cierre_id) REFERENCES public.usuarios(id);


--
-- Name: compra_items compra_items_compra_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compra_items
    ADD CONSTRAINT compra_items_compra_id_fkey FOREIGN KEY (compra_id) REFERENCES public.compras(id) ON DELETE CASCADE;


--
-- Name: compra_items compra_items_insumo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compra_items
    ADD CONSTRAINT compra_items_insumo_id_fkey FOREIGN KEY (insumo_id) REFERENCES public.insumos(id);


--
-- Name: compra_items compra_items_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compra_items
    ADD CONSTRAINT compra_items_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- Name: compras compras_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.compras
    ADD CONSTRAINT compras_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: gastos gastos_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.gastos
    ADD CONSTRAINT gastos_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: grupos_adicionales grupos_adicionales_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.grupos_adicionales
    ADD CONSTRAINT grupos_adicionales_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id) ON DELETE CASCADE;


--
-- Name: mermas mermas_insumo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mermas
    ADD CONSTRAINT mermas_insumo_id_fkey FOREIGN KEY (insumo_id) REFERENCES public.insumos(id);


--
-- Name: mermas mermas_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mermas
    ADD CONSTRAINT mermas_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- Name: mermas mermas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.mermas
    ADD CONSTRAINT mermas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: movimientos_caja movimientos_caja_caja_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_caja
    ADD CONSTRAINT movimientos_caja_caja_id_fkey FOREIGN KEY (caja_id) REFERENCES public.cajas(id);


--
-- Name: movimientos_caja movimientos_caja_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_caja
    ADD CONSTRAINT movimientos_caja_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: movimientos_inventario movimientos_inventario_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.movimientos_inventario
    ADD CONSTRAINT movimientos_inventario_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- Name: producto_variantes producto_variantes_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.producto_variantes
    ADD CONSTRAINT producto_variantes_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id) ON DELETE CASCADE;


--
-- Name: promociones promociones_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.promociones
    ADD CONSTRAINT promociones_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id) ON DELETE CASCADE;


--
-- Name: receta_items receta_items_insumo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receta_items
    ADD CONSTRAINT receta_items_insumo_id_fkey FOREIGN KEY (insumo_id) REFERENCES public.insumos(id);


--
-- Name: receta_items receta_items_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receta_items
    ADD CONSTRAINT receta_items_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id) ON DELETE CASCADE;


--
-- Name: receta_items receta_items_variante_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.receta_items
    ADD CONSTRAINT receta_items_variante_id_fkey FOREIGN KEY (variante_id) REFERENCES public.producto_variantes(id) ON DELETE CASCADE;


--
-- Name: registros_auditoria registros_auditoria_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.registros_auditoria
    ADD CONSTRAINT registros_auditoria_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- Name: venta_item_adicionales venta_item_adicionales_adicional_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_adicionales
    ADD CONSTRAINT venta_item_adicionales_adicional_id_fkey FOREIGN KEY (adicional_id) REFERENCES public.adicionales(id) ON DELETE SET NULL;


--
-- Name: venta_item_adicionales venta_item_adicionales_venta_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_adicionales
    ADD CONSTRAINT venta_item_adicionales_venta_item_id_fkey FOREIGN KEY (venta_item_id) REFERENCES public.venta_items(id) ON DELETE CASCADE;


--
-- Name: venta_item_insumos venta_item_insumos_insumo_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_insumos
    ADD CONSTRAINT venta_item_insumos_insumo_id_fkey FOREIGN KEY (insumo_id) REFERENCES public.insumos(id);


--
-- Name: venta_item_insumos venta_item_insumos_venta_item_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_item_insumos
    ADD CONSTRAINT venta_item_insumos_venta_item_id_fkey FOREIGN KEY (venta_item_id) REFERENCES public.venta_items(id) ON DELETE CASCADE;


--
-- Name: venta_items venta_items_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items
    ADD CONSTRAINT venta_items_producto_id_fkey FOREIGN KEY (producto_id) REFERENCES public.productos(id);


--
-- Name: venta_items venta_items_promocion_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items
    ADD CONSTRAINT venta_items_promocion_id_fkey FOREIGN KEY (promocion_id) REFERENCES public.promociones(id) ON DELETE SET NULL;


--
-- Name: venta_items venta_items_variante_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items
    ADD CONSTRAINT venta_items_variante_id_fkey FOREIGN KEY (variante_id) REFERENCES public.producto_variantes(id) ON DELETE SET NULL;


--
-- Name: venta_items venta_items_venta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.venta_items
    ADD CONSTRAINT venta_items_venta_id_fkey FOREIGN KEY (venta_id) REFERENCES public.ventas(id) ON DELETE CASCADE;


--
-- Name: ventas ventas_anulada_por_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_anulada_por_id_fkey FOREIGN KEY (anulada_por_id) REFERENCES public.usuarios(id);


--
-- Name: ventas ventas_caja_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_caja_id_fkey FOREIGN KEY (caja_id) REFERENCES public.cajas(id);


--
-- Name: ventas ventas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.ventas
    ADD CONSTRAINT ventas_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id) ON DELETE SET NULL;


--
-- PostgreSQL database dump complete
--

