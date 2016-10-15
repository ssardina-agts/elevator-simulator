package au.edu.rmit.elevatorsim.action;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.json.JSONObject;

import au.edu.rmit.elevatorsim.Direction;
import au.edu.rmit.elevatorsim.WrapperModel;

public class ChangeDestinationAction extends Action
{
	private WrapperModel model;
	private int carId;
	private int floorId;
	private Direction nextDirection;

	public ChangeDestinationAction(long actionId, WrapperModel model, JSONObject params)
	{
		super(actionId, model.getEventQueue());
		this.model = model;
		this.carId = params.getInt("car");
		this.floorId = params.getInt("floor");
		
		switch (params.getString("nextDirection"))
		{
			case "up":
				this.nextDirection = Direction.UP;
				break;
			case "down":
				this.nextDirection = Direction.DOWN;
				break;
			default:
				this.nextDirection = Direction.NONE;
				break;
		}
	}

	@Override
	protected ProcessingStatus performAction()
	{
		Floor floor = model.getFloor(floorId);
		Car car = model.getCar(carId);
		
		if (car == null || floor == null || nextDirection == Direction.NONE)
		{
			failureReason = "invalid params";
			return ProcessingStatus.FAILED;
		}

		// check car is in transit
		if (model.getNextDirection(carId) == Direction.NONE)
		{
			failureReason = "Car " + carId + " not in transit";
			return ProcessingStatus.FAILED;
		}
		
		float currentHeight = car.getHeight();
		float lastDestHeight = car.getDestination().getHeight();
		float newDestHeight = floor.getHeight();
		Direction currDirection = (currentHeight < lastDestHeight) ?
				Direction.UP : Direction.DOWN;
		
		if (currDirection == Direction.UP)
		{
			if (newDestHeight < currentHeight)
			{
				failureReason = "Car " + carId + " already past floor " + floorId;
				return ProcessingStatus.FAILED;
			}
		}
		else
		{
			if (newDestHeight > currentHeight)
			{
				failureReason = "Car " + carId + " already past floor " + floorId;
				return ProcessingStatus.FAILED;
			}
		}
		
		car.setDestination(floor);
		model.setNextDirection(carId, nextDirection);
		return ProcessingStatus.IN_PROGRESS;
	}
}
