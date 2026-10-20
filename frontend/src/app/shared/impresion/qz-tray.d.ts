// qz-tray no publica tipos propios (es un UMD sin @types). Se declara acá solo la porción
// de la API que usa ImpresionTermica, no la librería completa.
declare module 'qz-tray' {
  interface QzPrintConfig {
    [clave: string]: unknown;
  }

  interface QzRawDato {
    type: string;
    format: string;
    flavor: string;
    data: string;
    options?: Record<string, unknown>;
  }

  const qz: {
    websocket: {
      connect(opciones?: Record<string, unknown>): Promise<void>;
      disconnect(): Promise<void>;
      isActive(): boolean;
    };
    printers: {
      find(buscar?: string): Promise<string[]>;
    };
    configs: {
      create(impresora: string, opciones?: Record<string, unknown>): QzPrintConfig;
    };
    print(config: QzPrintConfig, datos: (string | QzRawDato)[]): Promise<void>;
    security: {
      setCertificatePromise(
        resolver: (resolve: (valor: string) => void, reject: (error: unknown) => void) => void,
      ): void;
      setSignatureAlgorithm(algoritmo: string): void;
      setSignaturePromise(
        cerrar: (
          mensaje: string,
        ) => (resolve: (valor: string) => void, reject: (error: unknown) => void) => void,
      ): void;
    };
  };

  export = qz;
}
