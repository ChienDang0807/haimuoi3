import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Testimonial } from '../../shared/interfaces';

@Injectable({
  providedIn: 'root'
})
export class TestimonialService {
  getTestimonials(): Observable<Testimonial[]> {
    const testimonials: Testimonial[] = [
      {
        id: '1',
        quote: '"Giao diện web cực kỳ mượt, tìm món gì cũng thấy. Từ đồ gia dụng đến đồ công nghệ đều được phân loại thông minh, giúp mình tiết kiệm rất nhiều thời gian mua sắm."',
        authorName: 'Chiến Đặng',
        authorTitle: 'Software Engineer',
        authorLocation: 'Hà Nội',
        isHighlighted: false
      },
      {
        id: '2',
        quote: '"Haimuoi2 làm mình bất ngờ về tốc độ giao hàng và khâu đóng gói. Sản phẩm được bọc kỹ, nguyên seal, cảm giác bóc hộp chuyên nghiệp như mua tại store quốc tế vậy."',
        authorName: 'Thanh Tuyến',
        authorTitle: 'Content Creator',
        authorLocation: 'Đà Nẵng',
        isHighlighted: true
      },
      {
        id: '3',
        quote: '"Điều mình trân trọng nhất là dịch vụ hậu mãi. Đội ngũ hỗ trợ cực kỳ nhiệt tình, giải đáp mọi thắc mắc về kỹ thuật ngay lập tức. Rất đáng tin cậy!"',
        authorName: 'Thanh Cao',
        authorTitle: 'Art Director',
        authorLocation: 'TP. Hồ Chí Minh',
        isHighlighted: false
      }
    ];

    return of(testimonials);
  }
}
