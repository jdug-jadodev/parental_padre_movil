export type ValorJson = null | boolean | number | string | ValorJson[] | { [clave: string]: ValorJson };

export class MensajeInvalido extends Error {}
export function exigir(condicion: boolean, detalle: string): asserts condicion {
  if (!condicion) throw new MensajeInvalido(detalle);
}

export function utf8Estricto(bytes: Uint8Array): string {
  try {
    return new TextDecoder('utf-8', { fatal: true, ignoreBOM: true }).decode(bytes);
  } catch {
    throw new MensajeInvalido('UTF-8 inválido');
  }
}

// JSON.parse solo se usa en tokens de cadena; nunca se le entrega un objeto con duplicados.
export function leerJsonEstricto(texto: string): ValorJson {
  let posicion = 0;
  const actual = (): string => texto[posicion] ?? '';
  const espacios = (): void => { while (/^[ \t\r\n]$/.test(actual())) posicion++; };
  const consumir = (caracter: string): boolean => {
    if (actual() !== caracter) return false;
    posicion++;
    return true;
  };
  const cadena = (): string => {
    exigir(consumir('"'), 'Se esperaba una cadena JSON');
    const inicio = posicion - 1;
    while (posicion < texto.length) {
      const caracter = texto[posicion++]!;
      if (caracter === '"') {
        let decodificada: string;
        try { decodificada = JSON.parse(texto.slice(inicio, posicion)) as string; }
        catch { throw new MensajeInvalido('Escape de cadena inválido'); }
        exigir(/^[\x20-\x7e]*$/.test(decodificada), 'Solo se admite ASCII imprimible');
        return decodificada;
      }
      exigir(caracter >= ' ', 'Control no permitido en cadena');
      if (caracter === '\\') {
        exigir(posicion < texto.length, 'Escape JSON incompleto');
        posicion++;
      }
    }
    throw new MensajeInvalido('Cadena JSON sin cierre');
  };
  const valor = (profundidad: number): ValorJson => {
    exigir(profundidad <= 32, 'JSON demasiado profundo');
    espacios();
    if (consumir('{')) {
      const campos: Record<string, ValorJson> = Object.create(null) as Record<string, ValorJson>;
      espacios();
      if (consumir('}')) return campos;
      do {
        espacios();
        const clave = cadena();
        exigir(!Object.hasOwn(campos, clave), 'Clave JSON duplicada');
        espacios();
        exigir(consumir(':'), 'Falta separador de campo');
        campos[clave] = valor(profundidad + 1);
        espacios();
        if (consumir('}')) return campos;
      } while (consumir(','));
      throw new MensajeInvalido('Objeto JSON sin cierre');
    }
    if (consumir('[')) {
      const elementos: ValorJson[] = [];
      espacios();
      if (consumir(']')) return elementos;
      do {
        elementos.push(valor(profundidad + 1));
        espacios();
        if (consumir(']')) return elementos;
      } while (consumir(','));
      throw new MensajeInvalido('Array JSON sin cierre');
    }
    if (actual() === '"') return cadena();
    for (const [literal, resultado] of [['true', true], ['false', false], ['null', null]] as const) {
      if (texto.startsWith(literal, posicion)) { posicion += literal.length; return resultado; }
    }
    const inicio = posicion;
    while (/^[0-9]$/.test(actual())) posicion++;
    exigir(posicion > inicio, 'Valor JSON no permitido');
    const literal = texto.slice(inicio, posicion);
    exigir(literal === '0' || !literal.startsWith('0'), 'Número con ceros iniciales');
    const numero = Number(literal);
    exigir(Number.isSafeInteger(numero), 'Entero JSON no seguro');
    return numero;
  };
  const resultado = valor(0);
  espacios();
  exigir(posicion === texto.length, 'Contenido después del JSON');
  return resultado;
}

export function canonico(valor: unknown, profundidad = 0): string {
  exigir(profundidad <= 32, 'JSON demasiado profundo');
  if (valor === null || typeof valor === 'boolean') return JSON.stringify(valor);
  if (typeof valor === 'number') {
    exigir(Number.isSafeInteger(valor) && valor >= 0 && !Object.is(valor, -0), 'Entero JSON no seguro');
    return String(valor);
  }
  if (typeof valor === 'string') {
    exigir(/^[\x20-\x7e]*$/.test(valor), 'Solo se admite ASCII imprimible');
    return JSON.stringify(valor);
  }
  if (Array.isArray(valor)) return '[' + valor.map((it: unknown) => canonico(it, profundidad + 1)).join(',') + ']';
  exigir(typeof valor === 'object' && valor !== null, 'Valor no representable en CP/1');
  return '{' + Object.keys(valor).sort().map((clave) =>
    canonico(clave) + ':' + canonico((valor as Record<string, unknown>)[clave], profundidad + 1),
  ).join(',') + '}';
}
