import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Policy } from '../../shared/interfaces';

@Injectable({
  providedIn: 'root'
})
export class PolicyService {
  getMainPolicies(): Observable<Policy[]> {
    const policies: Policy[] = [
      {
        id: '1',
        title: 'Chính Sách Đổi Trả',
        icon: 'swap_horiz',
        items: [
          {
            label: 'Đổi trả linh hoạt',
            description: 'Hỗ trợ đổi trả miễn phí trong vòng 15 ngày kể từ ngày nhận hàng nếu có lỗi từ nhà sản xuất.'
          },
          {
            label: 'Điều kiện đơn giản',
            description: 'Sản phẩm còn nguyên tem mác, chưa qua sử dụng và có hóa đơn mua hàng.'
          }
        ]
      },
      {
        id: '2',
        title: 'Chính Sách Vận Chuyển',
        icon: 'local_shipping',
        items: [
          {
            label: 'Miễn phí vận chuyển',
            description: 'Áp dụng cho mọi đơn hàng từ 500.000đ trở lên trên toàn quốc.'
          },
          {
            label: 'Giao hàng hỏa tốc',
            description: 'Nhận hàng trong 2h - 4h đối với các khu vực nội thành.'
          }
        ]
      }
    ];

    return of(policies);
  }

  getBenefitPolicies(): Observable<Policy[]> {
    const policies: Policy[] = [
      {
        id: '3',
        title: 'Chính Sách Đổi Trả',
        icon: 'history',
        description: 'Đổi trả sản phẩm lỗi kỹ thuật trong 7 ngày.'
      },
      {
        id: '4',
        title: 'Giao Hàng Toàn Quốc',
        icon: 'public',
        description: 'Giao hàng nhanh từ 1-3 ngày làm việc.'
      },
      {
        id: '5',
        title: 'Ưu Đãi Đặc Quyền',
        icon: 'percent',
        description: 'Giảm ngay 10% cho đơn hàng đầu tiên.'
      },
      {
        id: '6',
        title: 'Cam Kết Chất Lượng',
        icon: 'verified_user',
        description: 'Sản phẩm chính hãng, bảo hành 12 tháng.'
      }
    ];

    return of(policies);
  }
}
