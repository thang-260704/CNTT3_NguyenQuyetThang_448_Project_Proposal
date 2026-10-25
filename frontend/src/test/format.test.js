import { formatPrice, STATUS_LABELS } from '../utils/format';

describe('format utils', () => {
  it('TC-FE-06: định dạng giá tiền Việt Nam', () => {
    expect(formatPrice(1250000)).toMatch(/^1\.250\.000\s?đ$/);
  });

  it('TC-FE-07: có nhãn tiếng Việt cho đủ 5 trạng thái đơn hàng', () => {
    expect(Object.keys(STATUS_LABELS)).toEqual(['PENDING', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED']);
  });
});
