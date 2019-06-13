package domain.service

import domain.TagDTO
import domain.repository.TagRepository

class TagService(private val tagRepository: TagRepository) {
    fun findAll(): TagDTO {
        tagRepository.findAll().let { tags ->
            return TagDTO(tags)
        }
    }
}