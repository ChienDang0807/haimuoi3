export interface Review {
  id: string;
  userName: string;
  userAvatar?: string;
  rating: number;
  comment: string;
  date: string;
  verified?: boolean;
}

export interface ReviewStats {
  averageRating: number;
  totalReviews: number;
  distribution: RatingDistribution;
}

export interface RatingDistribution {
  [key: number]: number;
}
