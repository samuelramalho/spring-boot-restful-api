package br.com.rs.demo.api.util;

import java.util.Set;

import javax.json.JsonMergePatch;
import javax.json.JsonPatch;
import javax.json.JsonStructure;
import javax.json.JsonValue;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.rs.demo.api.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class PatchHelper {

	private final ObjectMapper mapper;

    private final Validator validator;

    /**
     * Aplica um patch JSON em um objeto
     *
     * @param patch patch JSON
     * @param bean objeto que será atualizado
     * @param clazz  classe do objeto que será atualizado
     * @param <T>
     * @return objeto atualizado
     */
    public <T> T patch(JsonPatch patch, T bean, Class<T> clazz) {
        JsonStructure target = mapper.convertValue(bean, JsonStructure.class);
        JsonValue patched = applyPatch(patch, target);
        return convertAndValidate(patched, clazz);
    }

    /**
     * Aplica um merge patch JSON em um objeto
     *
     * @param patch merge patch JSON
     * @param bean objeto que será atualizado
     * @param clazz  classe do objeto que será atualizado
     * @param <T>
     * @return objeto atualizado
     */
    public <T> T mergePatch(JsonMergePatch patch, T bean, Class<T> clazz) {
        JsonValue target = mapper.convertValue(bean, JsonValue.class);
        JsonValue patched = applyMergePatch(patch, target);
        return convertAndValidate(patched, clazz);
    }

    private JsonValue applyPatch(JsonPatch patch, JsonStructure target) {
        try {
            return patch.apply(target);
        } catch (Exception e) {
            throw new UnprocessableEntityException(e);
        }
    }

    private JsonValue applyMergePatch(JsonMergePatch mergePatch, JsonValue target) {
        try {
            return mergePatch.apply(target);
        } catch (Exception e) {
            throw new UnprocessableEntityException(e);
        }
    }

    private <T> T convertAndValidate(JsonValue jsonValue, Class<T> beanClass) {
        T bean = mapper.convertValue(jsonValue, beanClass);
        validate(bean);
        return bean;
    }

    private <T> void validate(T bean) {
        Set<ConstraintViolation<T>> violations = validator.validate(bean);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
