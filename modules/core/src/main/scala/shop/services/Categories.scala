package shop.services

import shop.domain.category

trait Categories[F[_]] {
  def findAll: F[List[category.Category]]
  def create(name: category.CategoryName): F[category.CategoryId]
  
}
