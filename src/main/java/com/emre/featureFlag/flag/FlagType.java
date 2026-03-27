package com.emre.featureFlag.flag;


// Boolean flag is 0 for off, 100 for on. 0 if the flag percentage is 10 for example, 10 percent of the users get that flag
public enum FlagType {
    BOOLEAN,
    MULTIVARIATE
}
