package io.spring.articleservice.infrastructure.mybatis.readservice;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;

// MyBatis read service for tags - returns all tag names
@Mapper
public interface TagReadService {
  List<String> allTags();
}
