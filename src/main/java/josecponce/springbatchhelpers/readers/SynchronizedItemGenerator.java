package josecponce.springbatchhelpers.readers;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.util.function.Function;

public class SynchronizedItemGenerator<T> extends ItemGenerator<T> {

    public SynchronizedItemGenerator(Function<Long, T> generator, Long max) {
        super(generator, max);
    }

    @Override
    public synchronized T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return super.read();
    }
}
