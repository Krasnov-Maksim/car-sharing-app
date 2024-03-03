package mate.academy.carsharing.service;

import java.util.List;
import mate.academy.carsharing.dto.CreateEntityRequestDto;
import mate.academy.carsharing.dto.EntityResponseDto;
import org.springframework.data.domain.Pageable;

public interface EntityService<
        RequestDto extends CreateEntityRequestDto, ResponseDto extends EntityResponseDto> {

    ResponseDto save(RequestDto requestDto);

    void deleteById(Long id);

    ResponseDto updateById(Long id, RequestDto requestDto);

    ResponseDto getById(Long id);

    List<ResponseDto> findAll(Pageable pageable);
}
