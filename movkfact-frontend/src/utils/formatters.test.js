import { formatRowCount } from './formatters';

describe('formatRowCount', () => {
  it('retourne "—" pour null', () => {
    expect(formatRowCount(null)).toBe('—');
  });

  it('retourne "—" pour undefined', () => {
    expect(formatRowCount(undefined)).toBe('—');
  });

  it('retourne "0" pour 0', () => {
    expect(formatRowCount(0)).toBe('0');
  });

  it('retourne la valeur en string pour < 1000', () => {
    expect(formatRowCount(1)).toBe('1');
    expect(formatRowCount(999)).toBe('999');
    expect(formatRowCount(42)).toBe('42');
  });

  it('retourne formaté en fr-FR pour >= 1000 et < 1_000_000', () => {
    expect(formatRowCount(1000)).toBe((1000).toLocaleString('fr-FR'));
    expect(formatRowCount(12500)).toBe((12500).toLocaleString('fr-FR'));
    expect(formatRowCount(999999)).toBe((999999).toLocaleString('fr-FR'));
  });

  it('retourne le format M pour >= 1_000_000', () => {
    expect(formatRowCount(1000000)).toBe('1.0M');
    expect(formatRowCount(1200000)).toBe('1.2M');
    expect(formatRowCount(5500000)).toBe('5.5M');
  });

  it('retourne 1 décimale pour les millions', () => {
    expect(formatRowCount(1050000)).toBe('1.1M'); // 1.05 → toFixed(1) → 1.1
    expect(formatRowCount(10000000)).toBe('10.0M');
  });
});
