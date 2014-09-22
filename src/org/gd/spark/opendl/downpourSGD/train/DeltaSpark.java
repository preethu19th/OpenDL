/*
 * Copyright 2013 GuoDing
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gd.spark.opendl.downpourSGD.train;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import scala.Tuple2;

import org.apache.spark.api.java.function.Function;
import org.gd.spark.opendl.downpourSGD.SGDTrainConfig;
import org.gd.spark.opendl.downpourSGD.SampleVector;
import org.gd.spark.opendl.util.MathUtil;
import org.jblas.DoubleMatrix;

final class DeltaSpark implements Function<Tuple2<Integer, Iterable<SampleVector>>, DeltaSpark> {
    private static final long serialVersionUID = 1L;
    private SGDBase sgd;
    private SGDTrainConfig trainConfig;
    private SGDParam my_param;
    private int curr_epoch = 0;

    public DeltaSpark(SGDBase _sgd, SGDTrainConfig config, int epoch) {
        this.sgd = _sgd;
        this.trainConfig = config;
        this.curr_epoch = epoch;
        this.my_param = this.sgd.getParam().dup();
    }

    public SGDParam getParam() {
    	return this.my_param;
    }

    @Override
    public DeltaSpark call(Tuple2<Integer, Iterable<SampleVector>> arg) throws Exception {
        List<SampleVector> myList = new ArrayList<SampleVector>();
        for (SampleVector v: arg._2()) {
            myList.add(v);
        }
        Collections.shuffle(myList);
        
        DoubleMatrix x_samples = MathUtil.convertX2Matrix(myList);
        DoubleMatrix y_samples = null;
        if(this.sgd.isSupervise()) {
        	y_samples = MathUtil.convertY2Matrix(myList);
        }

        // check whether we use cg this time
        if (this.trainConfig.isUseCG() && (this.curr_epoch <= this.trainConfig.getCgEpochStep())) {
        	this.sgd.gradientUpdateCG(trainConfig, x_samples, y_samples, my_param);
        } else {
        	this.sgd.gradientUpdateMiniBatch(trainConfig, x_samples, y_samples, my_param);
        }
        return this;
    }
}
