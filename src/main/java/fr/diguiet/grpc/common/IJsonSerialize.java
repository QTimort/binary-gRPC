package fr.diguiet.grpc.common;

/**
 * Implements a json class serialization
 */
public interface IJsonSerialize {
    /**
     * Convert an instance into a Json representation
     * @return a Json serialized representation an instance
     */
    public String toJsonString();
}
